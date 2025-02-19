package utc.englishlearning.Encybara.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import utc.englishlearning.Encybara.domain.User;
import utc.englishlearning.Encybara.domain.response.auth.ResCreateUserDTO;
import utc.englishlearning.Encybara.domain.response.ResUpdateUserDTO;
import utc.englishlearning.Encybara.domain.response.ResUserDTO;
import utc.englishlearning.Encybara.domain.response.ResultPaginationDTO;
import utc.englishlearning.Encybara.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleCreateUser(User user) {

        return this.userRepository.save(user);
    }

    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    public User fetchUserById(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        return null;
    }

    public ResultPaginationDTO fetchAllUser(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());

        rs.setMeta(mt);

        // remove sensitive data
        List<ResUserDTO> listUser = pageUser.getContent()
                .stream().map(item -> this.convertToResUserDTO(item))
                .collect(Collectors.toList());

        rs.setResult(listUser);

        return rs;
    }

    public User handleUpdateUser(User reqUser) {
        User currentUser = this.fetchUserById(reqUser.getId());
        if (currentUser != null) {
            currentUser.setName(reqUser.getName());
            currentUser.setPhone(reqUser.getPhone());
            currentUser.setSpeciField(reqUser.getSpeciField());
            currentUser.setAvatar(reqUser.getAvatar());
            currentUser.setEnglishlevel(reqUser.getEnglishlevel());
            currentUser = this.userRepository.save(currentUser);
        }
        return currentUser;
    }

    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        ResCreateUserDTO res = new ResCreateUserDTO();

        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setPassword(user.getPassword());
        res.setPhone(user.getPhone());
        res.setSpeciField(user.getSpeciField());
        res.setAvatar(user.getAvatar());
        res.setEnglishlevel(user.getEnglishlevel());

        return res;
    }

    public ResUpdateUserDTO convertToResUpdateUserDTO(User user) {
        ResUpdateUserDTO res = new ResUpdateUserDTO();

        res.setId(user.getId());
        res.setName(user.getName());
        res.setPhone(user.getPhone());
        res.setSpeciField(user.getSpeciField());
        res.setAvatar(user.getAvatar());
        res.setEnglishlevel(user.getEnglishlevel());

        return res;
    }

    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO res = new ResUserDTO();

        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setPhone(user.getPhone());
        res.setSpeciField(user.getSpeciField());
        res.setAvatar(user.getAvatar());
        res.setEnglishlevel(user.getEnglishlevel());

        return res;
    }

    public void updateUserToken(String token, String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndEmail(String token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(token, email);
    }

    public boolean updateUserPassword(String email, String password) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setPassword(password);
            userRepository.save(currentUser);
            return true;
        }
        return false;
    }

    public void invalidateTokens(String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            // Xóa hoặc vô hiệu hóa token
            currentUser.setRefreshToken(null);
            this.userRepository.save(currentUser);
        }
    }

}
