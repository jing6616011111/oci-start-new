// OCI Start - 前端工具
console.log('OCI Start 已初始化');

function doAction(url, method, data) {
    const options = {
        method: method,
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    };

    if (method === 'POST' && data) {
        const params = new URLSearchParams();
        Object.keys(data).forEach(key => params.append(key, data[key]));
        options.body = params.toString();
    }

    fetch(url, options)
        .then(res => res.json())
        .then(json => {
            if (json.code === 200) {
                location.reload();
            } else {
                alert('错误：' + (json.message || '未知错误'));
            }
        })
        .catch(err => {
            console.error('操作失败：', err);
            alert('操作失败：' + err.message);
        });
}

// 租户表单提交
const tenantForm = document.getElementById('tenantForm');
if (tenantForm) {
    tenantForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const formData = new FormData(this);
        fetch('/tenant/saveWithKey', {
            method: 'POST',
            body: formData
        })
        .then(res => res.json())
        .then(json => {
            if (json.code === 200) {
                window.location.href = '/tenant/list';
            } else {
                alert('错误：' + (json.message || '保存失败'));
            }
        })
        .catch(err => alert('错误：' + err.message));
    });
}

const bootForm = document.getElementById('bootForm');
if (bootForm) {
    bootForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const formData = new FormData(this);
        fetch('/boot/save', {
            method: 'POST',
            body: formData
        })
        .then(res => res.json())
        .then(json => {
            if (json.code === 200) {
                location.reload();
            } else {
                alert('错误：' + (json.message || '保存失败'));
            }
        })
        .catch(err => alert('错误：' + err.message));
    });
}

function updateCustomName(id) {
    const customName = prompt('请输入自定义名称');
    if (customName === null) return;
    doAction('/tenant/updateCustomName', 'POST', { id, customName });
}

function updateCost(id) {
    const cost = prompt('请输入成本字段');
    if (cost === null) return;
    doAction('/tenant/updateCost', 'POST', { id, cost });
}

function openTenantStream(url) {
    const log = document.getElementById('tenantStreamLog');
    if (!log) return;
    log.classList.remove('d-none');
    log.textContent = '';

    const source = new EventSource(url);
    const append = (event) => {
        log.textContent += event.data + '\n';
        log.scrollTop = log.scrollHeight;
    };

    source.addEventListener('tenant-status', append);
    source.addEventListener('audit', append);
    source.addEventListener('complete', (event) => {
        append(event);
        source.close();
    });
    source.onerror = () => {
        log.textContent += '连接已结束或发生错误\n';
        source.close();
    };
}
