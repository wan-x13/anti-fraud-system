package antifraud.entity;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    private long id;
    private String name;
    private String username;
    private String role;

    public static UserDTO mapToUserDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getUsername(), user.getRole());
    }
}
