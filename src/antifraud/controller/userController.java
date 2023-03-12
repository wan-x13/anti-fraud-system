package antifraud.controller;


import antifraud.entity.User;
import antifraud.entity.UserDTO;
import antifraud.repository.UserRepository;
import antifraud.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class userController {

    private final UserRepository userRepository;
    @Autowired
    private  final UserDetailsServiceImpl userDetailsService;


    @GetMapping("/list")
    public List<UserDTO> getAllUser(){
        return userDetailsService.getAllUser();
    }

    @PostMapping("/user")
    public ResponseEntity<?> addUser(@Validated @RequestBody  User user){
        if(user.getUsername() == null || user.getName() == null || user.getPassword() == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<UserDTO> list =userDetailsService.getAllUser().stream().
                filter(it->it.getRole().equals("ADMINISTRATOR"))
                .findAny();
        if(list.isPresent()){
            user.setRole("MERCHANT");
            user.setAccountNonLocked(false);
        }
        else {
            user.setRole("ADMINISTRATOR");
            user.setAccountNonLocked(true);
        }
        var newUser = userDetailsService.saveUser(user);
        return new  ResponseEntity<>(UserDTO.mapToUserDTO(user), HttpStatus.CREATED);
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username){
        userDetailsService.deleteUserByUsername(username);
        return ResponseEntity.ok(
               new LinkedHashMap<>(
                       Map.of(
                               "username", username,
                               "status","Deleted successfully!"
                       )
               )
        );
    }
    @PutMapping("/role")
    public ResponseEntity<?> updateRole(@RequestBody UpdateRole role){
        if(role.getRole().equals("ADMINISTRATOR")){
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        else {
            userDetailsService.updateRoleUer(role.getUsername(), role.getRole());
            Optional<UserDTO> list =userDetailsService.getAllUser().stream().
                    filter(it->it.getUsername().equals(role.getUsername()))
                    .findAny();

            assert list.isPresent();
         var user = list.get();
            return new ResponseEntity<>(user, HttpStatus.OK);
         }
    }
    @PutMapping("/access")
    public ResponseEntity<?> unLockOrLock(@RequestBody Operation operation){
        var user = userRepository.findUserByusername(operation.getUsername()).orElseThrow(
                ()->new UsernameNotFoundException("User "+operation.getUsername()+" is Not found")
        );
        assert user!=null;
        if(user.getRole().equals("ADMINISTRATOR")){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        userDetailsService.unLockAccount(operation);
        String opt = operation.getOperation().equals("UNLOCK") ? "unlocked!" : "locked!";
        return ResponseEntity.ok(
               Map.of("status" , "User "+operation.getUsername()+" "+opt)
        );
    }
}
