export const isValidUsername = (str: string) => ['admin', 'editor'].indexOf(str.trim()) >= 0;

export const isExternal = (path: string) => /^(https?:|mailto:|tel:)/.test(path);

/**
 * 校验邮箱字符串
 * @returns 是否合法
 */
export const isValidEmail = (str: string) => {
    return /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/.test(str)
}
/**
 * 校验手机号
 * @param value 手机号字符串
 * @returns 是否合法
 */
export const isCellPhone = (value: string): boolean => {
    return /^1(3|4|5|6|7|8|9)\d{9}$/.test(value)
}
/**
 * 校验身份证号（18位）
 * @param value 身份证号字符串
 * @returns 是否合法
 */
export const isValidID = (value: string): boolean => {
    if (!value) return false

    // 1. 基本格式：17位数字 + 1位数字或X/x
    const reg = /^\d{17}(\d|X|x)$/
    if (!reg.test(value)) return false

    // 2. 校验出生日期
    const birth = value.substring(6, 14)
    const year = parseInt(birth.substring(0, 4))
    const month = parseInt(birth.substring(4, 6))
    const day = parseInt(birth.substring(6, 8))

    const date = new Date(year, month - 1, day)
    if (
        date.getFullYear() !== year ||
        date.getMonth() !== month - 1 ||
        date.getDate() !== day ||
        year < 1900 ||
        year > new Date().getFullYear()
    ) {
        return false
    }

    // 3. 校验最后一位校验码
    const factor = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
    const parity = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
    let sum = 0
    for (let i = 0; i < 17; i++) {
        sum += parseInt(value.charAt(i)) * factor[i]
    }
    const checkCode = parity[sum % 11]

    return checkCode === value.charAt(17).toUpperCase()
}

