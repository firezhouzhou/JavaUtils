package com.example.log.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 访问日志实体类
 */
@Entity
@Table(name = "access_log", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_request_time", columnList = "request_time"),
    @Index(name = "idx_ip_address", columnList = "ip_address")
})
@ApiModel(description = "访问日志")
public class AccessLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "日志ID")
    private Long id;
    
    @Column(name = "user_id")
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    
    @Column(name = "username", length = 100)
    @ApiModelProperty(value = "用户名")
    private String username;
    
    @Column(name = "request_method", length = 10)
    @ApiModelProperty(value = "请求方法")
    private String requestMethod;
    
    @Column(name = "request_url", length = 500)
    @ApiModelProperty(value = "请求URL")
    private String requestUrl;
    
    @Column(name = "request_params", columnDefinition = "TEXT")
    @ApiModelProperty(value = "请求参数")
    private String requestParams;
    
    @Column(name = "request_body", columnDefinition = "TEXT")
    @ApiModelProperty(value = "请求体")
    private String requestBody;
    
    @Column(name = "response_status")
    @ApiModelProperty(value = "响应状态码")
    private Integer responseStatus;
    
    @Column(name = "response_body", columnDefinition = "TEXT")
    @ApiModelProperty(value = "响应体")
    private String responseBody;
    
    @Column(name = "ip_address", length = 50)
    @ApiModelProperty(value = "IP地址")
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    @ApiModelProperty(value = "用户代理")
    private String userAgent;
    
    @Column(name = "execution_time")
    @ApiModelProperty(value = "执行时间(毫秒)")
    private Long executionTime;
    
    @Column(name = "request_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "请求时间")
    private LocalDateTime requestTime;
    
    @Column(name = "module_name", length = 50)
    @ApiModelProperty(value = "模块名称")
    private String moduleName;
    
    @Column(name = "operation_desc", length = 200)
    @ApiModelProperty(value = "操作描述")
    private String operationDesc;
    
    @Column(name = "exception_info", columnDefinition = "TEXT")
    @ApiModelProperty(value = "异常信息")
    private String exceptionInfo;
    
    // 构造函数
    public AccessLog() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRequestMethod() {
        return requestMethod;
    }
    
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }
    
    public String getRequestUrl() {
        return requestUrl;
    }
    
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }
    
    public String getRequestParams() {
        return requestParams;
    }
    
    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }
    
    public String getRequestBody() {
        return requestBody;
    }
    
    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }
    
    public Integer getResponseStatus() {
        return responseStatus;
    }
    
    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
    
    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public Long getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }
    
    public LocalDateTime getRequestTime() {
        return requestTime;
    }
    
    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }
    
    public String getModuleName() {
        return moduleName;
    }
    
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
    
    public String getOperationDesc() {
        return operationDesc;
    }
    
    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc;
    }
    
    public String getExceptionInfo() {
        return exceptionInfo;
    }
    
    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }
}
