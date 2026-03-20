package com.gd.self.tool.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一接口返回结果类
 * @author guodun
 * @date 2026/3/19 16:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L; // 序列化版本号（规范要求）

    /**
     * 响应状态码枚举
     */
    @AllArgsConstructor
    @Getter
    public enum ResponseCode {
        SUCCESS(200, "操作成功"),
        PARAM_ERROR(400, "参数错误"),
        SERVER_ERROR(500, "服务器内部错误"),
        NOT_FOUND(404, "资源不存在"), // 补充常用状态码
        UNAUTHORIZED(401, "未授权"); // 补充常用状态码

        private final int code;
        private final String desc;
    }

    // 对外返回的数字状态码（前端更易处理）
    private Integer code;
    // 返回消息
    private String msg;
    // 泛型数据体
    private T data;

    // ==================== 成功响应方法 ====================
    /**
     * 无数据的成功响应（默认消息）
     */
    public static <T> Result<T> success() {
        return success(null, ResponseCode.SUCCESS.getDesc());
    }

    /**
     * 带数据的成功响应（默认消息）
     */
    public static <T> Result<T> success(T data) {
        return success(data, ResponseCode.SUCCESS.getDesc());
    }

    /**
     * 带数据+自定义消息的成功响应
     */
    public static <T> Result<T> success(T data, String msg) {
        return buildResult(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    // ==================== 失败响应方法 ====================
    /**
     * 默认失败响应（服务器内部错误）
     */
    public static <T> Result<T> fail() {
        return fail(ResponseCode.SERVER_ERROR);
    }

    /**
     * 指定状态码的失败响应（用枚举，避免硬编码）
     */
    public static <T> Result<T> fail(ResponseCode responseCode) {
        return fail(responseCode, responseCode.getDesc());
    }

    /**
     * 指定状态码+自定义消息的失败响应
     */
    public static <T> Result<T> fail(ResponseCode responseCode, String msg) {
        return buildResult(responseCode.getCode(), msg, null);
    }

    /**
     * 仅自定义失败消息（默认500状态码）
     */
    public static <T> Result<T> fail(String msg) {
        return buildResult(ResponseCode.SERVER_ERROR.getCode(), msg, null);
    }

    // ==================== 通用构建方法（私有，封装核心逻辑） ====================
    /**
     * 统一构建响应结果的私有方法（避免重复代码）
     */
    private static <T> Result<T> buildResult(Integer code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}