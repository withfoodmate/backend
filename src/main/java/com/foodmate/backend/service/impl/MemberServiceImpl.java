package com.foodmate.backend.service.impl;

import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.service.MemberService;
import com.foodmate.backend.util.FileRandomNaming;
import com.foodmate.backend.util.S3Deleter;
import com.foodmate.backend.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.StringTokenizer;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;
    private final S3Deleter s3Deleter;
    private final String s3BucketFolderName = "profile-images/";



    /**
     * @param email
     * @return 현재 사용중인 email 이면 false 리턴
     *         아니면 true
     */
    @Override
    public Boolean checkDuplicateEmail(String email) {
        if(memberRepository.findByEmail(email).isPresent()){
            return false;
        }
        return true;
    }

    /**
     * @param email
     * @return 현재 사용중인 email 이면 false 리턴
     *         아니면 true
     */
    @Override
    public Boolean checkDuplicateEmail(String email) {
        if(memberRepository.findByEmail(email).isPresent()){
            return false;
        }
        return true;
    }

    /**
     * @param authentication
     * @param imageFile
     * 사용자에게 사진파일을 받아와 프로필 이미지 변경
     */
    @Transactional
    public String patchProfileImage(Authentication authentication, MultipartFile imageFile) throws IOException {
        /* 사용자가 없을 시 예외 처리 */
        Member member = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

       /* 기존 이미지가 null이 아니면,
       있던 프로필 정보를 삭제하기 위한 s3 삭제 */
        if (isProfileImage(member.getImage())) {
            s3Deleter.deleteObject(getImageObjectKey(member.getImage()));
        }

        /* 새로 받아온 사진을 UUID를 사용한 무작위의 파일명으로 변경 후 s3업로드 */
        uploadProfileImage(member, imageFile);
        return "프로필 사진 수정 완료";
    }


    /**
     *
     * @param memberImage
     * @return 기존에 이미지가 있으면 true 없으면 false
     *
     */
    private boolean isProfileImage(String memberImage){
        if(memberImage == null) {
            return false;
        }
        return true;
    }

    /**
     * @param imageUrl
     * @return s3에 있는 objectKey를 찾기위한 메서드
     */
    private String getImageObjectKey(String imageUrl){
        StringTokenizer st = new StringTokenizer(imageUrl,"/");
        StringBuilder objectKey = new StringBuilder(s3BucketFolderName);
        String str = "";
        while(st.hasMoreTokens()){
            str = st.nextToken();
        }
        objectKey.append(str);
        return objectKey.toString();
    }

    /**
     * 파일럽로드 메서드
     * @param member
     * @param imageFile
     * @throws IOException
     */
    private void uploadProfileImage(Member member, MultipartFile imageFile) throws IOException {
        member.setImage(
                s3Uploader.uploadAndGenerateUrl(
                        imageFile,
                        s3BucketFolderName +
                                FileRandomNaming.fileRandomNaming(imageFile))
        );
        memberRepository.save(member);
    }

}
