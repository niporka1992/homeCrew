import { useState } from 'react';
import { Form, Input, Button, Card, App } from 'antd';
import { api } from '../api/api.js';

export default function LoginPage() {
    const [loading, setLoading] = useState(false);
    const { message } = App.useApp(); // ✅ теперь message берётся из контекста

    const onFinish = async (values) => {
        setLoading(true);
        try {
            const data = await api.post('/auth/login', values);

            localStorage.setItem('token', data.token);
            message.success('Добро пожаловать!');
            window.location.href = '/';
        } catch (e) {
            message.error(e.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            height: '100vh'
        }}>
            <Card title="Вход в HomeCrew" style={{ width: 360 }}>
                <Form layout="vertical" onFinish={onFinish}>
                    <Form.Item
                        label="Имя пользователя"
                        name="username"
                        rules={[{ required: true, message: 'Введите имя пользователя' }]}
                    >
                        <Input placeholder="Введите логин" />
                    </Form.Item>
                    <Form.Item
                        label="Пароль"
                        name="password"
                        rules={[{ required: true, message: 'Введите пароль' }]}
                    >
                        <Input.Password placeholder="Введите пароль" />
                    </Form.Item>
                    <Button
                        type="primary"
                        htmlType="submit"
                        block
                        loading={loading}
                    >
                        Войти
                    </Button>
                </Form>
            </Card>
        </div>
    );
}
