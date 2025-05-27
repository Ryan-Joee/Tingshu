package com.ryan.handler;

import com.ryan.exception.TingshuException;
import com.ryan.result.ResultCodeEnum;
import com.ryan.result.RetVal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 全局异常处理类
 *
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 自定义异常处理方法
     * @param e 异常对象
     * @return RetVal
     */
    @ExceptionHandler(TingshuException.class)
    @ResponseBody
    public RetVal error(TingshuException e){
        return RetVal.build(null,e.getCode(), e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseBody
    public RetVal llegalArgumentException(Exception e) {
        log.error("触发异常拦截: " + e.getMessage(), e);
        return RetVal.build(null, ResultCodeEnum.ARGUMENT_VALID_ERROR);
    }

    /**
     * spring security异常
     * @param e
     * @return
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public RetVal error(AccessDeniedException e) throws AccessDeniedException {
        return RetVal.build(null, ResultCodeEnum.PERMISSION);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public RetVal error(Exception e){
        e.printStackTrace();
        return RetVal.fail();
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public RetVal handleThrowable(Throwable t) {
        t.printStackTrace();
        return RetVal.fail("系统错误：" + t.getMessage());
    }



}
