package kr.co.codea.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/users")
    public String getUsers(Model model) {
        List<UserDTO> users = userMapper.getAllUsers();
        model.addAttribute("users", users);
        return "userList";  // templates/userList.html
    }
}