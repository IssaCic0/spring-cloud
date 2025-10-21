/**
 * 时间戳
 * @param {*} timestamp  时间戳
 */
const timestampToTime = (timestamp) => {
    let date = new Date(timestamp) //时间戳为10位需*1000，时间戳为13位的话不需乘1000
    let Y = date.getFullYear() + '-'
    let M =
        (date.getMonth() + 1 < 10 ?
            '0' + (date.getMonth() + 1) :
            date.getMonth() + 1) + '-'
    let D =
        (date.getDate() < 10 ? '0' + date.getDate() : date.getDate()) + ' '
    let h =
        (date.getHours() < 10 ? '0' + date.getHours() : date.getHours()) + ':'
    let m =
        (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes()) +
        ':'
    let s =
        date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds()
    return Y + M + D + h + m + s
};

/**
 * 中文枚举映射过滤器
 */
const ROLE_LABEL = { BUYER: '买家', MERCHANT: '商家', ADMIN: '管理员' };
const SHOP_STATUS_LABEL = { PENDING_APPROVAL: '待审核', OPEN: '营业中', CLOSED: '已关闭', BANNED: '已封禁' };
const PRODUCT_STATUS_LABEL = { ACTIVE: '上架', OFFLINE: '下架' };
const ORDER_STATUS_LABEL = { CREATED: '已创建', PAID: '已支付', CANCELLED: '已取消', SHIPPED: '已发货', DELIVERED: '已收货', REFUNDING: '退款中', REFUNDED: '已退款' };
const SHIP_STATUS_LABEL = { CREATED: '待发货', SHIPPED: '已发货', DELIVERED: '已收货' };
const PAYMENT_STATUS_LABEL = { PENDING: '待支付', PAID: '已支付', FAILED: '失败', REFUNDED: '已退款' };
const PROMO_TYPE_LABEL = { COUPON: '优惠券', DISCOUNT: '折扣' };
const PROMO_STATUS_LABEL = { ACTIVE: '进行中', VOIDED: '已作废', ENDED: '已结束' };

const fmtRole = v => ROLE_LABEL[v] || v;
const fmtShopStatus = v => SHOP_STATUS_LABEL[v] || v;
const fmtProductStatus = v => PRODUCT_STATUS_LABEL[v] || v;
const fmtOrderStatus = v => ORDER_STATUS_LABEL[v] || v;
const fmtShipStatus = v => SHIP_STATUS_LABEL[v] || v;
const fmtPaymentStatus = v => PAYMENT_STATUS_LABEL[v] || v;
const fmtPromotionType = v => PROMO_TYPE_LABEL[v] || v;
const fmtPromotionStatus = v => PROMO_STATUS_LABEL[v] || v;
/**
 * 存储localStorage
 */
const setStore = (name, content) => {
    if (!name) return;
    if (typeof content !== 'string') {
        content = JSON.stringify(content);
    }
    window.localStorage.setItem(name, content);
}

/**
 * 获取localStorage
 */
const getStore = name => {
    if (!name) return;
    return window.localStorage.getItem(name);
}

/**
 * 删除localStorage
 */
const removeStore = name => {
    if (!name) return;
    window.localStorage.removeItem(name);
}

/**
 * 设置cookie
 **/
function setCookie(name, value, day) {
    let date = new Date();
    date.setDate(date.getDate() + day);
    document.cookie = name + '=' + value + ';expires=' + date;
};

/**
 * 获取cookie
 **/
function getCookie(name) {
    let reg = RegExp(name + '=([^;]+)');
    let arr = document.cookie.match(reg);
    if (arr) {
        return arr[1];
    } else {
        return '';
    }
};

/**
 * 删除cookie
 **/
function delCookie(name) {
    setCookie(name, null, -1);
};

/**
 * 导出 
 **/
export {
    timestampToTime,
    setStore,
    getStore,
    removeStore,
    setCookie,
    getCookie,
    delCookie,
    fmtRole,
    fmtShopStatus,
    fmtProductStatus,
    fmtOrderStatus,
    fmtShipStatus,
    fmtPaymentStatus,
    fmtPromotionType,
    fmtPromotionStatus
}