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
        const data = {};
        formData.forEach((v, k) => data[k] = v);
        fetch('/tenant/save', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
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
