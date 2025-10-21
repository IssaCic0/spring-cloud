# coding=utf-8
import os
import sys
import pandas as pd
from sqlalchemy import create_engine
from sqlalchemy import inspect
from pyecharts.charts import Pie
from pyecharts import options as opts
from pyecharts.globals import ThemeType

# 优先使用 snapshot-selenium，更稳定；若未安装则回退到 snapshot-phantomjs
try:
    from snapshot_selenium import snapshot
except Exception:
    from snapshot_phantomjs import snapshot  # 可能需要安装 phantomjs
from pyecharts.render import make_snapshot


def drawImage(args):
    print("------ start to draw (pie) -------")
    if len(args) < 3:
        print("用法: python pie.py <保存目录> <文件名> [dimension=product|category|shop] [start] [end] [topN]")
        return

    save_dir = args[1]
    file_name = args[2]
    dim = args[3] if len(args) > 3 and args[3] else 'product'
    start = args[4] if len(args) > 4 and args[4] and args[4] != '_' else None
    end = args[5] if len(args) > 5 and args[5] and args[5] != '_' else None
    try:
        top_n = int(args[6]) if len(args) > 6 and args[6] else 10
    except Exception:
        top_n = 10
    metric = args[7] if len(args) > 7 and args[7] else 'sales'  # sales | inventory

    if not file_name.lower().endswith('.png'):
        file_name += '.png'

    os.makedirs(save_dir, exist_ok=True)
    out_path = os.path.join(save_dir, file_name)
    print(f"输出路径: {out_path}, 维度: {dim}, 起止: {start} ~ {end}, TopN: {top_n}")

    # 数据库连接 (与 Spring Boot 配置一致)
    database_config = 'mysql+pymysql://root:123456@localhost:3306/onlineshop?charset=utf8mb4'
    engine = create_engine(database_config)
    # 若选择了库存统计，但数据库中不存在 inventories 表，则自动回退到销量统计
    try:
        inspector = inspect(engine)
        if metric == 'inventory' and not inspector.has_table('inventories'):
            print("inventories 表不存在，自动切换到销量统计(sales)")
            metric = 'sales'
    except Exception as e:
        if metric == 'inventory':
            print(f"检查 inventories 表失败，回退到销量统计，原因: {e}")
            metric = 'sales'

    # 动态 SQL 构建
    params = {}
    if metric == 'inventory':
        # 按库存占比统计
        label_expr = "p.title"
        group_expr = "p.title"
        joins = "JOIN products p ON p.id = i.product_id"
        title = "库存占比"
        if dim == 'category':
            label_expr = "COALESCE(p.category, '未分类')"
            group_expr = "p.category"
            title = "各品类库存占比"
        elif dim == 'shop':
            joins += " JOIN shops s ON s.id = p.shop_id"
            label_expr = "s.name"
            group_expr = "s.name"
            title = "各店铺库存占比"

        where = []
        if start:
            where.append("i.updated_at >= %(start)s")
            params['start'] = start
        if end:
            where.append("i.updated_at < DATE_ADD(%(end)s, INTERVAL 1 DAY)")
            params['end'] = end
        where_sql = (" WHERE " + " AND ".join(where)) if where else ""

        sql = (
            f"SELECT {label_expr} AS label, SUM(i.quantity) AS total "
            f"FROM inventories i {joins}"
            f"{where_sql} "
            f"GROUP BY {group_expr} "
            f"ORDER BY total DESC "
            f"LIMIT {int(top_n)}"
        )
    else:
        # 按销量占比统计（默认）
        label_expr = "p.title"
        group_expr = "p.title"
        joins = "JOIN products p ON p.id = oi.product_id"
        title = "热销商品销量占比"
        if dim == 'category':
            label_expr = "COALESCE(p.category, '未分类')"
            group_expr = "p.category"
            title = "各品类销量占比"
        elif dim == 'shop':
            joins += " JOIN shops s ON s.id = oi.shop_id"
            label_expr = "s.name"
            group_expr = "s.name"
            title = "各店铺销量占比"

        where = [
            "EXISTS (SELECT 1 FROM payments pay WHERE pay.order_id = oi.order_id AND pay.status='PAID')"
        ]
        if start:
            where.append("oi.created_at >= %(start)s")
            params['start'] = start
        if end:
            where.append("oi.created_at < DATE_ADD(%(end)s, INTERVAL 1 DAY)")
            params['end'] = end

        sql = (
            f"SELECT {label_expr} AS label, SUM(oi.quantity) AS total "
            f"FROM order_items oi {joins} "
            f"WHERE {' AND '.join(where)} "
            f"GROUP BY {group_expr} "
            f"ORDER BY total DESC "
            f"LIMIT {int(top_n)}"
        )

    df = pd.read_sql(sql, engine, params=params)
    print("------ 查询结果 ------")
    print(df)

    if df.empty:
        print("无数据，直接返回")
        return

    data_pairs = list(zip(df['label'].tolist(), df['total'].tolist()))

    # 使用浅色主题，避免与前端深色背景冲突
    pie = Pie(init_opts=opts.InitOpts(theme=ThemeType.LIGHT, width='800px', height='600px'))
    pie.add(
        "销量占比",
        data_pairs,
        radius=["30%", "70%"],
        label_opts=opts.LabelOpts(formatter="{b}: {d}%", position="outside")
    ).set_global_opts(
        title_opts=opts.TitleOpts(title=title, pos_left="center"),
        legend_opts=opts.LegendOpts(pos_left="left", orient="vertical")
    )

    print(f"开始生成饼图，数据: {data_pairs}")
    html_path = pie.render()
    print(f"HTML文件生成: {html_path}")
    make_snapshot(snapshot, html_path, out_path)
    print(f"图片生成完成: {out_path}")
    print("OK draw pie is over")


if __name__ == "__main__":
    drawImage(sys.argv)
