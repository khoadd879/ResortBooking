Ứng dụng **đặt resort / khách sạn trực tuyến** với các chức năng quản lý phòng, dịch vụ, người dùng và đặt phòng.  
Dự án này hướng đến việc xây dựng một hệ thống **quản lý booking resort hiện đại** với trải nghiệm tốt cho cả khách hàng và quản trị viên.

---

## ✨ Tính năng

- 🔑 Xác thực & phân quyền người dùng (User / Manager / Admin)  
- 🏨 Quản lý resort và danh sách phòng  
- 🛏️ Đặt phòng theo ngày check-in / check-out  
- 📦 Thêm dịch vụ đi kèm (ăn uống, xe đưa đón, …)  
- 💳 Thanh toán và quản lý chi tiết hóa đơn  
- 📊 Trang admin: quản lý, duyệt hoặc hủy booking  
- ❤️ Yêu thích resort / phòng (favorites)  
- 🔎 Tìm kiếm, lọc, phân trang  

---

## 🏗️ Công nghệ sử dụng

- **Ngôn ngữ:** Kotlin (Android + Backend)  
- **Backend:** Spring Boot 
- **Database:** PostgreSQL 
- **API giao tiếp:** RESTful + JWT Authentication  
- **Frontend (Mobile):** Android (Kotlin, XML Layout, ViewModel, LiveData, Retrofit, Coroutine)  
- **Thư viện chính:**  
  - Retrofit2 – gọi API  
  - Glide – load ảnh  
  - Coroutine – xử lý bất đồng bộ  
  - ViewModel + LiveData – quản lý state  
---

## 🚀 Cài đặt & Chạy dự án

### Yêu cầu
- JDK 17+  
- Android Studio (nếu chạy app mobile)  
- PostgreSQL / MySQL đã cài đặt sẵn  

### Backend
```bash
git clone https://github.com/khoadd879/ResortBooking.git
cd ResortBooking/backend
./gradlew bootRun
