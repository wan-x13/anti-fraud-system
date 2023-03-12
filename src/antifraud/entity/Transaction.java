package antifraud.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "\"transaction\"")
@Data
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @Column
    private Long amount;
    @Column
    private String ip ;
    @Column
    private String number;
    @Column
    private String region;
    @Column
    private LocalDateTime date;
    @Column
    private String result ;
    @Column
    private String feedBack;

    @JsonProperty("transactionId")
    public Long getId() {
        return id;
    }
    @JsonProperty("result")
    public String getResult() {
        return result == null ? "" : result;
    }
    @JsonProperty("feedback")
    public String getFeedBack() {
        return feedBack == null ? "": feedBack;
    }
}
