# Package
### controller
- MemberController
- GroupController
- EnrollmentController
- ChatRoomController
- RankingController

### entity
- Member
- Group
- Enrollment
- Preference
- Food
- Comment
- Reply
- ChatRoom
- ChatMember
- ChatMessage

### exception
- GlobalException
- MemberException
- GroupException
- EnrollmentException
- PreferenceException
- FoodException
- CommentException
- ReplyException
- ChatException

### repository
- MemberRepository
- GroupRepository
- EnrollmentRepository
- PreferenceRepository
- FoodRepository
- CommentRepository
- ReplyRepository
- ChatRoomRepository
- ChatMemberRepository
- ChatMessageRepository

### service
- MemberService
- GroupService
- EnrollmentService
- ChatRoomService
- RankingService
- **serviceImpl**
    - MemberServiceImpl
    - GroupServiceImpl
    - EnrollmentServiceImpl
    - ChatRoomServiceImpl
    - RankingServiceImpl

### enums
- MemberRole
- MemberLoginType
- EnrollmentStatus
- GroupStatus
- Error

### configure
- S3Config
- RedisConfig
- SwaggerConfig

### security
- config
- dto
- exception
- filter
- service

### util
- DateParser
- S3Uploader
- S3Deleter
- FileRandomNaming

### dto
- MemberDto
- GroupDto
- EnrollmentDto
- CommentDto
- ReplyDto

# Commit Message Conventions
- chore : 빌드 환경 설정, 패키지 매니저 수정(ex .gitignore 수정 같은 경우)
- docs : 문서 수정
- feat : 새로운 기능 추가
- bug : 버그 수정
- style : 스타일 관련 기능(코드 포맷팅, 세미콜론 누락, 코드 자체의 변경이 없는 경우)
- refactor : 코드 리펙토링
- revert : 이전 커밋 되돌리기
- test : 테스트 코드

# Git flow process

1. **작업할 Branch를 `origin/develop` Branch를 기반으로 아래와 같은 이름으로 생성한다.**
    - feature/<개발 기능>-<Issue 번호>              ex) feature/login-#1
    - ex) ![image](https://github.com/withfoodmate/frontend/assets/96711699/59c32e5f-9f99-4f2c-ac0d-328fe0665da6)
2. **1번에서 생성한 Branch를 Checkout하고 Push한다.**
3. **해당 Branch에서 개발 작업을 하고 세분화해서 Commit Message Conventions에 맞게 Commit 후 Push를 한다.**
4. **모든 개발 작업이 완료되었다면, 해당 Branch를 `origin/develop` Branch로 Pull Request(이하 PR)를 아래와 같은 형식으로 생성한다.**
    - Title: FOODMATE <Issue 번호> <개발 작업을 포괄하는 제목>                   ex) FOODMATE #1 PR 템플릿 추가
    - Content: PR 작성 가이드를 참고해서 내용을 작성
    - Reviewers: 백엔드 팀원 모두 선택
    - Assignees: 본인
    - Labels: 개발 작업을 대표하는 심볼을 선택
    - ex) ![image](https://github.com/withfoodmate/frontend/assets/96711699/b4c16f4a-194f-4be6-9ac1-b319fec49299)

5. **모든 Reviewer가 코드 리뷰를 완료하면, PR 생성자가 Merge를 진행하고 해당 Branch를 삭제한다.**
    - 마지막 Reviewer는 PR 생성자를 멘션하여 PR 생성자에게 모든 코드 리뷰가 끝났다는 것을 알려준다.