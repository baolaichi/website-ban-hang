💻 LaptopShop - Hệ thống Thương mại Điện tử Thông minh

Đồ án Tốt nghiệp: Xây dựng website bán hàng điện tử tích hợp Chatbot AI và Hệ thống gợi ý thông minh.

📖 Giới thiệu

LaptopShop là một nền tảng thương mại điện tử hiện đại chuyên kinh doanh các thiết bị điện tử (Laptop, PC, Phụ kiện). Hệ thống không chỉ cung cấp các chức năng mua sắm cơ bản mà còn tích hợp các công nghệ tiên tiến như Chatbot AI (Rasa) để tư vấn tự động và Hệ thống gợi ý (Recommendation System) để cá nhân hóa trải nghiệm người dùng.

🚀 Tính năng nổi bật

1. Phía Khách hàng (Client)

Mua sắm thông minh: Tìm kiếm, Lọc sản phẩm, Xem chi tiết, Thêm vào giỏ hàng (AJAX).

Thanh toán đa dạng: Hỗ trợ thanh toán khi nhận hàng (COD) và thanh toán trực tuyến an toàn qua VNPAY.

Chatbot AI (Rasa): Hỗ trợ tư vấn 24/7, tra cứu giá sản phẩm, kiểm tra trạng thái đơn hàng thông qua hội thoại tự nhiên.

Gợi ý sản phẩm:

Khách vãng lai: Gợi ý sản phẩm bán chạy nhất.

Thành viên: Gợi ý sản phẩm dựa trên sở thích cá nhân (Lọc cộng tác).

Quản lý tài khoản: Theo dõi lịch sử đơn hàng, hủy đơn, đổi mật khẩu.

2. Phía Quản trị (Admin)

Dashboard: Thống kê doanh thu, đơn hàng mới, biểu đồ tăng trưởng thực tế.

Quản lý: Sản phẩm, Danh mục, Đơn hàng (Cập nhật trạng thái, Xử lý), Khách hàng.

Hiệu năng: Tích hợp Redis Caching để tăng tốc độ tải trang và giảm tải cho CSDL.

🛠️ Công nghệ sử dụng

Backend: Java 17, Spring Boot (Spring MVC, Spring Security, Spring Data JPA).

Frontend: Thymeleaf, HTML5, CSS3, Bootstrap 5, jQuery.

Database: MySQL 8.0.

Caching: Redis.

AI/Chatbot: Python, Rasa Framework.

Payment: VNPAY Sandbox.

⚙️ Hướng dẫn Cài đặt & Chạy dự án

Yêu cầu tiên quyết

Java JDK 17+

Maven

MySQL Server

Docker (để chạy Redis) hoặc cài Redis trực tiếp

Python 3.8+ (để chạy Rasa)

Bước 1: Cài đặt Cơ sở dữ liệu & Redis

Tạo database MySQL tên là laptopshop.

Mở file src/main/resources/application.properties và cập nhật thông tin MySQL của bạn.

Khởi chạy Redis (Sử dụng Docker):

docker run -d --name redis-server -p 6379:6379 redis


Bước 2: Cài đặt Chatbot (Rasa)

Di chuyển vào thư mục chứa code Rasa (ví dụ: rasa-bot).

Cài đặt thư viện:

pip install rasa requests


Khởi chạy Rasa Server (Cần mở 2 terminal):

Terminal 1 (Action Server):

rasa run actions


Terminal 2 (NLU Server):

rasa run --enable-api --cors "*"


Bước 3: Chạy ứng dụng Spring Boot

Tại thư mục gốc dự án, chạy lệnh:

mvn spring-boot:run


Truy cập website tại: http://localhost:8080

Truy cập trang Admin tại: http://localhost:8080/admin (Cần tài khoản Role ADMIN).

🤝 Đóng góp (Git Flow)

Dự án tuân thủ quy trình Git Flow cơ bản:

master: Nhánh chính thức, chứa code ổn định để deploy.

develop: Nhánh phát triển, chứa các tính năng mới nhất đang được tích hợp.

feature/*: Các nhánh tính năng riêng biệt (ví dụ: feature/payment-vnpay, feature/chatbot).

📄 Bản quyền

Đồ án thuộc về [Tên của bạn] - [Mã sinh viên] - [Lớp].


---

### PHẦN 2: HƯỚNG DẪN QUY TRÌNH GIT FLOW

* Bạn đang ở nhánh `master`.
* Bạn vừa commit trực tiếp vào `master` (`update new`).

**Vấn đề:** Trong làm việc nhóm hoặc dự án chuyên nghiệp, **không nên commit trực tiếp vào master**. `master` chỉ dùng để chứa code đã hoàn thiện (Production Ready).

Dưới đây là quy trình bạn nên làm từ bây giờ:

#### 1. Sơ đồ các nhánh
* `master`: Code sạch, chạy ổn định (Chỉ merge từ `release` hoặc `develop` vào, không code trực tiếp).
* `develop`: Nhánh chính để phát triển. Mọi tính năng mới sẽ được merge vào đây.
* `feature/ten-tinh-nang`: Nhánh con để làm từng chức năng (ví dụ: `feature/cart`, `feature/vnpay`).

#### 2. Quy trình thực hiện (Ví dụ bạn muốn sửa code CartService)

**Bước 1: Đừng ở master, hãy chuyển sang develop**
```bash
git checkout develop
git pull origin develop  # Cập nhật code mới nhất về


Bước 2: Tạo một nhánh mới từ develop để làm việc

git checkout -b feature/fix-cart-service


(Lúc này bạn đang ở nhánh feature/fix-cart-service, bạn tha hồ sửa code, phá code mà không sợ ảnh hưởng đến ai).

Bước 3: Code, Add và Commit (như bạn đã làm)

git add .
git commit -m "Fix: update logic cart service and header"


Bước 4: Đẩy nhánh feature lên Github

git push origin feature/fix-cart-service


Bước 5: Merge vào develop (Hoặc tạo Pull Request trên Github)
Nếu làm một mình, bạn có thể merge trực tiếp:

git checkout develop
git merge feature/fix-cart-service
git push origin develop


(Sau khi merge xong, nhánh develop đã có code mới).

Bước 6: Đưa lên master (Khi mọi thứ đã ổn định)

git checkout master
git merge develop
git push origin master
