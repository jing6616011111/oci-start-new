#!/usr/bin/env bash
set -euo pipefail

SERVICE_NAME="oci-start"
INSTALL_DIR="/opt/oci-start"
SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}.service"

if [[ "${EUID}" -ne 0 ]]; then
  echo "请使用 root 权限运行：sudo ${0}"
  exit 1
fi

echo "==> 停止并禁用服务"
systemctl stop "${SERVICE_NAME}" 2>/dev/null || true
systemctl disable "${SERVICE_NAME}" 2>/dev/null || true

echo "==> 删除服务文件和程序目录"
rm -f "${SERVICE_FILE}"
rm -rf "${INSTALL_DIR}"
systemctl daemon-reload

echo "==> 卸载完成"
echo "数据目录 /var/lib/oci-start 未自动删除，如需清理请手动执行：sudo rm -rf /var/lib/oci-start"
