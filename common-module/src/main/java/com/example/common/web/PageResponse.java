package com.example.common.web;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 */
@ApiModel("分页响应结果")
public class PageResponse<T> implements Serializable {
    
    @ApiModelProperty("当前页码")
    private Long current;
    
    @ApiModelProperty("每页大小")
    private Long size;
    
    @ApiModelProperty("总记录数")
    private Long total;
    
    @ApiModelProperty("总页数")
    private Long pages;
    
    @ApiModelProperty("数据列表")
    private List<T> records;
    
    public PageResponse() {}
    
    public PageResponse(Long current, Long size, Long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records;
        this.pages = (total + size - 1) / size;
    }
    
    /**
     * 从Spring Data Page对象构造PageResponse
     */
    public PageResponse(Page<T> page) {
        this.current = (long) page.getNumber();
        this.size = (long) page.getSize();
        this.total = page.getTotalElements();
        this.records = page.getContent();
        this.pages = (long) page.getTotalPages();
    }
    
    // Getters and Setters
    public Long getCurrent() {
        return current;
    }
    
    public void setCurrent(Long current) {
        this.current = current;
    }
    
    public Long getSize() {
        return size;
    }
    
    public void setSize(Long size) {
        this.size = size;
    }
    
    public Long getTotal() {
        return total;
    }
    
    public void setTotal(Long total) {
        this.total = total;
    }
    
    public Long getPages() {
        return pages;
    }
    
    public void setPages(Long pages) {
        this.pages = pages;
    }
    
    public List<T> getRecords() {
        return records;
    }
    
    public void setRecords(List<T> records) {
        this.records = records;
    }
}