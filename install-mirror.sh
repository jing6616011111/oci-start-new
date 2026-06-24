#!/usr/bin/env bash
set -Eeuo pipefail

REPO="jing6616011111/oci-start-new"
PACKAGE_NAME="oci-start-linux.tar.gz"
DOWNLOAD_URL="${OCI_START_DOWNLOAD_URL:-https://github.com/${REPO}/releases/latest/download/${PACKAGE_NAME}}"

if [[ "${EUID}" -ne 0 ]]; then
  if ! command -v sudo >/dev/null 2>&1; then
    echo "请使用 root 用户运行，或先安装 sudo。"
    exit 1
  fi
  echo "==> 需要 root 权限，正在通过 sudo 重新执行"
  exec sudo -E bash -c "$(curl -fLsS "https://raw.githubusercontent.com/${REPO}/main/install-mirror.sh")"
fi

need_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "缺少命令：$1"
    return 1
  fi
}

install_base_tools() {
  if command -v curl >/dev/null 2>&1 && command -v tar >/dev/null 2>&1; then
    return
  fi

  echo "==> 安装基础工具"
  if command -v apt-get >/dev/null 2>&1; then
    apt-get update
    apt-get install -y curl tar ca-certificates
  elif command -v dnf >/dev/null 2>&1; then
    dnf install -y curl tar ca-certificates
  elif command -v yum >/dev/null 2>&1; then
    yum install -y curl tar ca-certificates
  else
    echo "无法自动安装基础工具，请先安装 curl、tar、ca-certificates。"
    exit 1
  fi
}

install_java() {
  if command -v java >/dev/null 2>&1; then
    return
  fi

  echo "==> 未检测到 Java，开始安装 OpenJDK"
  if command -v apt-get >/dev/null 2>&1; then
    apt-get update
    apt-get install -y openjdk-17-jre-headless
  elif command -v dnf >/dev/null 2>&1; then
    dnf install -y java-17-openjdk-headless
  elif command -v yum >/dev/null 2>&1; then
    yum install -y java-17-openjdk-headless || yum install -y java-11-openjdk-headless
  else
    echo "无法自动安装 Java，请先安装 Java 8 或更高版本。"
    exit 1
  fi
}

if ! command -v systemctl >/dev/null 2>&1; then
  echo "当前系统未检测到 systemd/systemctl，暂不支持一键安装为系统服务。"
  exit 1
fi

install_base_tools
install_java
need_cmd curl
need_cmd tar
need_cmd java

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

echo "==> 下载 OCI Start 安装包"
curl -fL --retry 3 --retry-delay 2 -o "${TMP_DIR}/${PACKAGE_NAME}" "${DOWNLOAD_URL}"

echo "==> 解压安装包"
tar -xzf "${TMP_DIR}/${PACKAGE_NAME}" -C "${TMP_DIR}"

echo "==> 执行安装"
cd "${TMP_DIR}/oci-start-linux"
bash ./install.sh

echo "==> OCI Start 安装完成"
echo "访问地址：http://服务器IP:9856"
echo "默认账号：admin"
echo "默认密码：admin123"
