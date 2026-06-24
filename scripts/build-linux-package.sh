#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_DIR="${ROOT_DIR}/dist"
PACKAGE_DIR="${DIST_DIR}/oci-start-linux"
PACKAGE_FILE="${DIST_DIR}/oci-start-linux.tar.gz"
JAR_FILE="${ROOT_DIR}/oci-server/target/oci-server-1.0.0-SNAPSHOT.jar"

cd "${ROOT_DIR}"

echo "==> 开始编译 OCI Start"
mvn clean package -DskipTests

echo "==> 生成 Linux 一键安装包"
rm -rf "${PACKAGE_DIR}" "${PACKAGE_FILE}"
mkdir -p "${PACKAGE_DIR}"

cp "${JAR_FILE}" "${PACKAGE_DIR}/oci-start.jar"
cp "${ROOT_DIR}/scripts/linux/install.sh" "${PACKAGE_DIR}/install.sh"
cp "${ROOT_DIR}/scripts/linux/uninstall.sh" "${PACKAGE_DIR}/uninstall.sh"
cp "${ROOT_DIR}/scripts/linux/oci-start.service" "${PACKAGE_DIR}/oci-start.service"
cp "${ROOT_DIR}/README.md" "${PACKAGE_DIR}/README.md"

chmod +x "${PACKAGE_DIR}/install.sh" "${PACKAGE_DIR}/uninstall.sh"

tar -C "${DIST_DIR}" -czf "${PACKAGE_FILE}" oci-start-linux

echo "==> 安装包已生成：${PACKAGE_FILE}"
