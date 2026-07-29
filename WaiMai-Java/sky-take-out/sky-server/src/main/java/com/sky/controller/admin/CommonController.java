package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;
    /**
     * 图片上传接口
     * POST/admin/common/upload
     */
    @PostMapping("/upload")
    @ApiOperation(value = "图片上传")
    public Result<String> upload(MultipartFile file){
        try{
            byte[] bytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName =  System.currentTimeMillis() + suffix;
            String url = aliOssUtil.upload(bytes, fileName);
            return Result.success(url);
        }catch (IOException e){
            log.error("上传失败", e);
            throw new BaseException(MessageConstant.UPLOAD_FAILED);
        }
    }
}

