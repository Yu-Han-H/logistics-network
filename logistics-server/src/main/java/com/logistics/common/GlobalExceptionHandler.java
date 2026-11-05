package com.logistics.common;

import com.logistics.exception.CityNotFoundException;
import com.logistics.exception.DuplicateCityException;
import com.logistics.exception.ForbiddenException;
import com.logistics.exception.InvalidCoordinateException;
import com.logistics.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理。所有 Controller 不写 try/catch，统一在这里转成 Result。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<Result<Void>> handleCityNotFound(CityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Result.<Void>fail(ResultCode.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(DuplicateCityException.class)
    public ResponseEntity<Result<Void>> handleDuplicateCity(DuplicateCityException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Result.<Void>fail(ResultCode.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(InvalidCoordinateException.class)
    public ResponseEntity<Result<Void>> handleInvalidCoordinate(InvalidCoordinateException ex) {
        return ResponseEntity.badRequest()
            .body(Result.<Void>fail(ResultCode.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result<Void>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Result.<Void>fail(ResultCode.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Result<Void>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Result.<Void>fail(ResultCode.FORBIDDEN, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> new FieldError(fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();
        return ResponseEntity.badRequest()
            .body(Result.<Void>fail(ResultCode.BAD_REQUEST, "参数校验失败", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(Result.<Void>fail(ResultCode.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpected(Exception ex) {
        log.error("未处理的异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Result.<Void>fail(ResultCode.INTERNAL_ERROR, "服务器内部错误"));
    }
}
