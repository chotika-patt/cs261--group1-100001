package tu_store.demo.services;

import org.springframework.stereotype.Service;
import tu_store.demo.models.User;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    // DB แบบ ยังไม่ใส่ sql เอาจริงต้องเชื่อมและใส่ใน repositories
    private final Map<String,String> users = new HashMap<>();

    public boolean register(User user){
        if(users.containsKey(user.getUsername())){
            return false;
        }
        users.put(user.getUsername(),user.getPassword());
        return true;
    }
    public boolean login(User user) {
        return users.containsKey(user.getUsername()) &&
                users.get(user.getUsername()).equals(user.getPassword());
    }
}
