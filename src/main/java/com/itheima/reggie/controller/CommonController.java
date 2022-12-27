package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author lijia
 * @create 2022-11-16 22:06
 */
@Slf4j
@RequestMapping("/common")
@RestController
public class CommonController {
    @Value("${reggie.file-path}")
    private String basePath;

    //文件上传
    @PostMapping("/upload")
    public R<String> upload(@RequestPart MultipartFile file) {
        //file只是一个临时文件，需要转存到服务器指定文件存储区
        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = uuid + suffix;
        try {
            file.transferTo(new File(basePath.concat(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    //文件下载
    @GetMapping("/download")
    public void download(@RequestParam String name, HttpServletResponse response) {
        File file = new File(basePath.concat(name));
        ServletOutputStream os = null;
        BufferedInputStream bis = null;
        try {
            //获取响应对象的写入流
            os = response.getOutputStream();
            bis = new BufferedInputStream(new FileInputStream(file));
            response.setContentType("image/jpeg");
            IOUtils.copy(bis, os);
//            int len;
//            byte bytes[] = new byte[1024];
//            while ((len = bis.read(bytes)) != -1) {
//                os.write(bytes, 0, len);
//                os.flush();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
