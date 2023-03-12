package antifraud.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "\"card\"")
public class Card {
    @Id
    @GeneratedValue(generator = "increment")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @Column
    private  String number;
    @Column
    @JsonIgnore
    private boolean isLocked;
    @JsonIgnore
    @Column
    private int maxAllowed = 200;
    @JsonIgnore
    @Column int maxManual = 1500;

}
