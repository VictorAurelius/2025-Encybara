Đầu tiên hãy đọc documents/req-1.md để hiểu về plan task 1 đã thực hiện.
Sau đó đọc các file output của plan task req-1 để hiểu kết quả đang có.

Tiếp theo, hãy giúp tôi tạo plan task req-2 theo các tiêu chí sau:
1. Tôi có thêm 2 file input là documents/API Document - SAMPLE.pdf và Testcase API - SAMPLE.pdf, hãy đọc sample này để hiểu thêm về cách để tạo ouput
2. đọc lại các file input trước đó để đủ context
3. dựa trên các context vừa tìm hiểu được, thực hiện sửa lại 2 file output hợp lý
4. file output phải đầy đủ, hợp lý và có thể dễ dàng paste dữ liệu vào excel


tôi muốn là 1 bảng tổng hợp chứ không phải update, hãy tạo req-3:
1. tạo output tổng hợp trong thư mục output
2. test case lần lượt như sample
3. hãy điều tra pronunciation-assessment-service để biết chính xác câu trúc response

tiếp tục tạo req-4 để test API gradeAnswer trong AnswerController

sửa lại re1-4 theo cấu trúc folder hiện tại (tôi mới cập nhật)

đọc documents/req-1.md, documents/req-2.md, documents/req-3.md, documents/req-4.md để hiểu context, sau đó hãy giúp tôi tạo plan task để thực hiện các nhiệm vụ sai=u:
1. sửa documents/output/Testcase_API_AssessPronunciation sử dụng data: 
file=documents/input/audio_sample.mp3
text= "Most of my peers go crazy about Vietnamese rap music 'cause it's in vogue, you know? I do listen to some Vietnamese rappers once in a while, but I gotta say my affinity with this type of music is not on par with that of my friends."

2. hãy đọc backend-service/test-pronunciation.sh để hiểu cách lấy token để test API

3. thực hiện tạo script để test theo testcase của documents/output/Testcase_API_AssessPronunciation 

Lưu ý: đây chỉ là plan task cho claude, không thực hiện test luôn

hãy đưa plan task này thành file req-5.md

tôi cần tạo plan task req-6 để test api Testcase_API_GradeAnswer:
1. hãy đọc API_Document_GradeAnswer và Testcase_API_GradeAnswer để hiểu về api chấm điểm câu trả lời dạng CHOICE và text

2. hãy đọc backend-service/src/main/java/.../config/AdminDataInitializer.java cùng với các file tham chiếu để hiều về logic seeding data cho các course khi khởi động hệ thống

3. hãy tạo 1 file scripts cho user@example.com:
+ tạo enrollment vào course placement 
+ tạo answer cho để test
+ thực hiện test case cho api grade answer

4. tạo lại Testcase_API_GradeAnswer cho khóa placement

Tóm lại, tôi cần 1 file scripts để test api grade answer tự động, từ lúc login đến tham gia khóa học placement đến tạo answer và grade answer, lưu ý chỉ grade cho các câu hỏi TEXT và CHOICE

Lưu ý: đây chỉ là plan task cho claude, không thực hiện test luôn