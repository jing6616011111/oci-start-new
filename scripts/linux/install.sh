#!/usr/bin/env bash
set -euo pipefail

SERVICE_NAME="oci-start"
INSTALL_DIR="/opt/oci-start"
DATA_DIR="/var/lib/oci-start"
LOG_DIR="/var/log/oci-start"
SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}.service"

if [[ "${EUID}" -ne 0 ]]; then
  echo "请使用 root 权限运行：sudo ./install.sh"
  exit 1
fi

if ! command -v java >/dev/null 2>&1; then
  echo "未检测到 java 命令。请先安装 Java 8 或更高版本。"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==> 创建目录"
mkdir -p "${INSTALL_DIR}" "${DATA_DIR}" "${LOG_DIR}"

echo "==> 安装程序文件"
cp "${SCRIPT_DIR}/oci-start.jar" "${INSTALL_DIR}/oci-start.jar"
cp "${SCRIPT_DIR}/uninstall.sh" "${INSTALL_DIR}/uninstall.sh"
chmod +x "${INSTALL_DIR}/uninstall.sh"

rm -rf "${INSTALL_DIR}/data"
ln -s "${DATA_DIR}" "${INSTALL_DIR}/data"

echo "==> 注册 systemd 服务"
cp "${SCRIPT_DIR}/oci-start.service" "${SERVICE_FILE}"
systemctl daemon-reload
systemctl enable "${SERVICE_NAME}"
systemctl restart "${SERVICE_NAME}"

echo "==> 安装完成"
echo "访问地址：http://服务器IP:9856"
echo "默认账号：admin"
echo "默认密码：admin123"
echo "查看状态：sudo systemctl status ${SERVICE_NAME}"
