package com.sky.controller.admin;


import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;


    @PostMapping("/upload")
    @ApiOperation("上传图片")
    public Result<String> upload(MultipartFile file){
        log.info("开始上传图片");
        try {
            String fileName = file.getOriginalFilename();
            String substring = fileName.substring(fileName.lastIndexOf("."));
            String objectName = UUID.randomUUID().toString()+substring;

            String fileUrl = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(fileUrl);
        } catch (IOException e) {
           log.error("文件上传失败:{}",e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
