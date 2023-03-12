package antifraud.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomError {
    private int statusCode;
    private LocalDateTime timeStamp;
    private  String error ;
    private  String description;

}
