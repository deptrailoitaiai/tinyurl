FROM alpine:3.19

# Cài đặt các CLI cần thiết
RUN apk update && \
    apk add --no-cache bash mysql-client redis curl && \
    apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime && \
    echo "Asia/Ho_Chi_Minh" > /etc/timezone && \
    apk del tzdata

# Chuẩn bị thư mục làm việc
WORKDIR /scripts

# Sẽ được mount từ host
VOLUME /scripts

# Đặt quyền thực thi khi container khởi động
CMD ["sh", "-c", "chmod +x /scripts/*.sh && /scripts/entrypoint.sh"]
