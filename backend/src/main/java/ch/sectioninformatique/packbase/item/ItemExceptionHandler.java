package ch.sectioninformatique.packbase.item;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ControllerAdvice
public class ItemExceptionHandler {

  @ExceptionHandler(ItemNotFoundException.class)
  @ResponseBody 
  @ResponseStatus(HttpStatus.NOT_FOUND)
  String itemNotFoundHandler(ItemNotFoundException e) {
    return e.getMessage();
  }
}
