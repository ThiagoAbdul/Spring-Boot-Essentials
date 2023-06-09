package com.estudos.springframework.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;


@Getter
@AllArgsConstructor
@Builder
public class BadRequestExceptionDetails extends ExceptionDetails{
    public BadRequestExceptionDetails(Exception e){
        super(e, HttpStatus.BAD_REQUEST);
    }


}

