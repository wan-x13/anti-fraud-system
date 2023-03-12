package antifraud.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "\"Ip_Address\"")
public class IpAddress{
    @Id
    @GeneratedValue(generator = "increment")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @Column
    private String ip;


}
