export const checkProcessEnv =() => {
  return process.env.VUE_APP_DELETE_PERMISSIONS==='true'
}
export const debounce=(fn, time)=> {
  time = time || 200
  // 定时器
  let timer = null
  return function(...args) {
    var _this = this
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(function() {
      timer = null
      fn.apply(_this, args)
    }, time)
  }
  
};
//节流
export const throttle = (fn, time)=> {
  let timer = null
  time = time || 1000
  return function(...args) {
    if (timer) {
      return
    }
    const _this = this
    timer = setTimeout(() => {
      timer = null
    }, time)
    fn.apply(_this, args)
  }
}
// 判断正、负
export const strIncrease = (str)=>{
  if(str.slice(0,1) ==='-'){
    return true
    }
}

// 格式化金额为两位小数
export const formatToFixed = (num: number | string): string => {
  if (num === null || num === undefined || num === '') {
    return '0.00'
  }
  const number = typeof num === 'string' ? parseFloat(num) : num
  if (isNaN(number)) {
    return '0.00'
  }
  return (Math.round(number * 100) / 100).toFixed(2)
}