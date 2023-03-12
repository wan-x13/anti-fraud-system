package antifraud.service;

import antifraud.controller.Operation;
import antifraud.entity.User;
import antifraud.entity.UserDTO;
import antifraud.exception.BadRequestException;
import antifraud.exception.ConflictException;
import antifraud.exception.NotInDatabaseException;
import antifraud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{

    private  final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findUserByusername(username).orElseThrow(
                ()->new UsernameNotFoundException("User "+username+" is Not found")
        );
       return new UserDetailImpl(user);
    }
    //@Override
    public User saveUser(User user) {
       if(userRepository.findAll().stream()
                .anyMatch(u->u.getUsername().equalsIgnoreCase(user.getUsername())) ){
           throw new ConflictException();
       }
        user.setUsername(user.getUsername().toLowerCase());
        user.setPassword(passwordEncoder().encode(user.getPassword()));
        UserDTO.mapToUserDTO(user);

        return userRepository.save(user);
    }
    //@Override
    public List<UserDTO> getAllUser() {
       return userRepository.findAll().stream().map(UserDTO::mapToUserDTO).toList();
    }
   // @Override
    public void deleteUserByUsername(String username) {
       var user = userRepository.findUserByusername(username.toLowerCase())
               .orElseThrow(NotInDatabaseException::new);
      userRepository.deleteById(user.getId());
    }
    public void updateRoleUer(String username , String role){

       var user =  userRepository.findUserByusername(username.toLowerCase())
                .orElseThrow(NotInDatabaseException::new);
       List<String> roles = List.of("SUPPORT","MERCHANT");

        if(user.getRole().equals(role)) {
            throw new ConflictException();
        }
        System.out.println(role);
        if(!roles.contains(role)){
            throw new BadRequestException();
        }
        user.setRole(role);
        userRepository.save(user);

    }
    public void unLockAccount(Operation operation){
        var user =  userRepository.findUserByusername(operation.getUsername().toLowerCase())
                .orElseThrow(NotInDatabaseException::new);
        assert user != null;
        user.setAccountNonLocked(operation.getOperation().equals("UNLOCK"));
        userRepository.save(user);
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
