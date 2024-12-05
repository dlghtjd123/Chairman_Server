package com.example.chairman_server.service.user;

import com.example.chairman_server.domain.Institution.Institution;
import com.example.chairman_server.domain.user.User;
import com.example.chairman_server.dto.Institution.InstitutionData;
import com.example.chairman_server.repository.Institution.InstitutionRepository;
import com.example.chairman_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Service
public class UserService {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;  // InstitutionRepository 주입 추가

    public User getUser(Long id) {
        Optional<User> user = this.userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new IllegalArgumentException("없는 유저 입니다.");
        }
    }

    // 사용자 정보 조회
    public Map<String, String> getUserInfo(String email) {
        Map<String, String> userInfo = new HashMap<>();

        // 사용자를 이메일로 조회
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 사용자 정보를 map에 저장
            userInfo.put("name", user.getName());
            userInfo.put("email", user.getEmail());
            userInfo.put("phoneNumber", user.getPhoneNumber());
            userInfo.put("address", user.getAddress());
        } else {
            // 사용자가 없으면 예외를 던질 수도 있습니다.
            userInfo.put("error", "사용자를 찾을 수 없습니다.");
        }

        return userInfo;
    }

    // 사용자 정보 수정
    public void updateUserInfo(String email, String name, String phoneNumber, String address) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        user.setName(name);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);

        userRepository.save(user);  // 변경된 정보 저장
    }

    //프로필 사진 업로드 메서드
    public String saveProfilePhoto(String email, MultipartFile photo) throws IOException {
        //이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //업로드 디렉토리 설정
        String uploadDir = "uploads/profile_photos/";
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        //파일 이름 생성 (이메일 + 파일 이름 조합)
        String fileName = email + "_" + photo.getOriginalFilename();
        File destinationFile = new File(uploadDir + fileName);

        //파일 저장
        photo.transferTo(destinationFile);

        //저장된 파일의 URL 생성
        String fileUrl = "/uploads/profile_photos/" + fileName;

        //사용자 엔티티에 프로필 이미지 URL 업데이트
        user.setProfileImageUrl(fileUrl);
        userRepository.save(user);

        return fileUrl;
    }

    // 모든 공공기관 조회
    public List<InstitutionData> getAllInstitutions() {
        List<Institution> institutions = institutionRepository.findAll();
        return institutions.stream()
                .map(institution -> new InstitutionData(
                        institution.getInstitutionId(),
                        institution.getName(),
                        institution.getTelNumber(),
                        institution.getInstitutionCode(),
                        institution.getAddress()))
                .collect(Collectors.toList());
    }

    public void clear() {
        userRepository.deleteAll();
    }
}
