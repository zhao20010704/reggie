package com.fox.reggie.controller;

import com.fox.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;


/**
 * 文件上传和下载
 */

@RestController
@RequestMapping("common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 图片文件上传
     * @param file
     * @return
     */
    /**
     1、获取文件的原始文件名, 通过原始文件名获取文件后缀
     2、通过UUID重新声明文件名, 文件名称重复造成文件覆盖
     3、创建文件存放目录
     4、将上传的临时文件转存到指定位置
     */
    @PostMapping("upload")
    public R<String> upload(MultipartFile file){
        //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        log.info(file.toString());
//        1、获取文件的原始文件名, 通过原始文件名获取文件后缀
        String originalFilename = file.getOriginalFilename();
        //截取字符串，输出文件后缀名
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
//        2、通过UUID重新声明文件名, 文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID().toString()+substring;
//        3、创建文件存放目录
        //创建一个目录对象
        File dir = new File(basePath);
        //判断目录是否存在
        if (!dir.exists()){
            //创建
            dir.mkdirs();
        }

//        4、将上传的临时文件转存到指定位置
        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
    1). 定义输入流，通过输入流读取文件内容
    2). 通过response对象，获取到输出流
    3). 通过response对象设置响应数据格式(image/jpeg)
    4). 通过输入流读取文件数据，然后通过上述的输出流写回浏览器
    5). 关闭资源
     */
    @GetMapping("download")
    public void download(String name, HttpServletResponse response){
        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //输出流，通过输出流将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
