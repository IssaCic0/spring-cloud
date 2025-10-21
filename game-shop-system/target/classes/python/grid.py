# coding=utf-8
import os
import sys
import pandas as pd
from sqlalchemy import create_engine
from pyecharts.charts import Bar, Line
from pyecharts import options as opts
from pyecharts.globals import ThemeType

try:
    from snapshot_selenium import snapshot
except Exception:
    from snapshot_phantomjs import snapshot
from pyecharts.render import make_snapshot


def drawImage(args):
    print("------ start to draw (grid: bar+line) -------")
    if len(args) < 3:
        print("用法: python grid.py <保存目录> <文件名>")
        return

    save_dir = args[1]
    file_name = args[2]

    if not file_name.lower().endswith('.png'):
        file_name += '.png'

    os.makedirs(save_dir, exist_ok=True)
    out_path = os.path.join(save_dir, file_name)
    print(f"输出路径: {out_path}")

    # 数据库连接 (与 Spring Boot 配置一致)
    database_config = 'mysql+pymysql://root:123456@localhost:3306/onlineshop?charset=utf8mb4'
    engine = create_engine(database_config)

    # 改为按"游戏名称"做横轴：取所有已支付订单的销量 Top10 的游戏
    sales_sql = (
        "SELECT p.title AS name, SUM(oi.quantity) AS sales "
        "FROM order_items oi "
        "JOIN products p ON p.id = oi.product_id "
        "JOIN payments pay ON pay.order_id = oi.order_id AND pay.status='PAID' "
        "GROUP BY p.title "
        "ORDER BY sales DESC"
    )
    sales_df = pd.read_sql(sales_sql, engine)
    if sales_df.empty:
        x_values = ["无数据"]
        sales_values = [0]
    else:
        sales_df = sales_df.head(10)
        x_values = sales_df['name'].tolist()
        sales_values = sales_df['sales'].astype(int).tolist()

    # 库存：当前数据库无 inventories 表，使用 0 作为占位，避免查询失败
    stock_values = [0 for _ in x_values]

    # 再次兜底
    if not x_values:
        x_values = ["无数据"]
        sales_values = [0]
        stock_values = [0]

    bar = Bar(init_opts=opts.InitOpts(width="800px", height="600px", theme=ThemeType.LIGHT))
    bar.add_xaxis(x_values)
    bar.add_yaxis("销量(件)", sales_values)
    bar.set_global_opts(
        xaxis_opts=opts.AxisOpts(type_="category", axislabel_opts=opts.LabelOpts(rotate=24)),
        yaxis_opts=opts.AxisOpts(type_="value", name="销量(件)", min_=0, max_=500)
    )

    # 右侧副轴显示库存
    bar.extend_axis(yaxis=opts.AxisOpts(type_="value", name="库存(件)", min_=0))

    line = Line()
    line.add_xaxis(x_values)
    line.add_yaxis(
        "库存(件)",
        stock_values,
        yaxis_index=1,
        is_smooth=False,
        linestyle_opts=opts.LineStyleOpts(width=2),
        symbol="circle",
        symbol_size=6
    )

    bar = bar.overlap(line)

    html_path = bar.render()
    make_snapshot(snapshot, html_path, out_path)
    print("OK grid draw is over")


if __name__ == "__main__":
    drawImage(sys.argv)
