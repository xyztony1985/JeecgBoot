/**
 * 构建 bizCode，格式：{table_name}.{field_name}
 * 用于托管模式下自动标识文件归属
 *
 * @param tableName 数据库表名
 * @param fieldName 字段名
 * @returns bizCode 字符串
 */
export const buildBizCode = (tableName: string, fieldName: string): string => {
  return `${tableName}.${fieldName}`;
};
