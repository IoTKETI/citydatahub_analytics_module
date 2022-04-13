package com.vaiv.analyticsManager.restFullApi.exception;

import com.vaiv.analyticsManager.common.utils.MakeUtil;
import net.sf.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {
    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Object httpMethodNotReadableException(Exception ex, WebRequest req) {
        JSONObject result = new JSONObject();
        result.put("type", "5000");
        result.put("detail", "REQUEST에 입력한 BODY를 처리 할 수 없습니다.(HttpMessageNotReadableException)");
        MakeUtil.printErrorLogger(ex, "HttpMessageNotReadableException");
        return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Object unknownException(Exception ex, WebRequest req) {
        JSONObject result = new JSONObject();
        result.put("type", "5000");
        result.put("detail", ex.toString());
        MakeUtil.printErrorLogger(ex, "unknownException");
        return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
    }
}
