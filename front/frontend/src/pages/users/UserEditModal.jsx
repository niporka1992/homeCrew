import { Modal, Form, Input, Select, Switch, Row, Col, Divider } from 'antd'
import { useEffect } from 'react'

const ROLE_OPTIONS = [
    { value: 'OWNER', label: 'Администратор' },
    { value: 'WORKER', label: 'Работник' },
    { value: 'GUEST', label: 'Гость' },
]

export default function UserEditModal({ open, user, saving, onCancel, onSave }) {
    const [form] = Form.useForm()

    useEffect(() => {
        if (open && user) {
            form.setFieldsValue({
                fullName: user.fullName ?? '',
                phone: user.phone ?? '',
                email: user.email ?? '',
                role: user.role ?? 'GUEST',
                isBlocked: user.isBlocked ?? user.blocked ?? false,
                newPassword: '',
            })
        } else {
            form.resetFields()
        }
    }, [open, user, form])

    const handleOk = async () => {
        try {
            const values = await form.validateFields()
            await onSave(values)
        } catch {}
    }

    return (
        <Modal
            title={`👤 ${user ? user.username : 'Редактирование пользователя'}`}
            open={open}
            onCancel={onCancel}
            cancelText={"Отмена"}
            onOk={handleOk}
            okText="Сохранить"
            confirmLoading={saving}
            destroyOnClose
            centered
            width={520}
        >
            <Form form={form} layout="vertical" requiredMark={false}>
                <Row gutter={16}>
                    <Col span={12}>
                        <Form.Item label="Полное имя" name="fullName">
                            <Input placeholder="ФИО" allowClear />
                        </Form.Item>
                    </Col>

                    <Col span={12}>
                        <Form.Item
                            label="Роль"
                            name="role"
                            rules={[{ required: true, message: 'Укажите роль' }]}
                        >
                            <Select options={ROLE_OPTIONS} />
                        </Form.Item>
                    </Col>
                </Row>

                <Row gutter={16}>
                    <Col span={12}>
                        <Form.Item
                            label="Телефон"
                            name="phone"
                            rules={[
                                {
                                    validator: (_, value) => {
                                        if (!value || value.trim() === '') return Promise.resolve()
                                        const cleaned = value.replace(/[\s-()]/g, '')
                                        const ru = /^(?:\+7|8)\d{10}$/
                                        const intl = /^\+\d{6,15}$/
                                        if (ru.test(cleaned) || intl.test(cleaned))
                                            return Promise.resolve()
                                        return Promise.reject(
                                            new Error('Введите корректный номер')
                                        )
                                    },
                                },
                            ]}
                        >
                            <Input
                                type="tel"
                                inputMode="numeric"
                                placeholder="+7 999 123 45 67"
                                allowClear
                                onChange={(e) => {
                                    const val = e.target.value
                                    if (val.startsWith('8'))
                                        form.setFieldValue('phone', val.replace(/^8/, '+7'))
                                }}
                            />
                        </Form.Item>
                    </Col>

                    <Col span={12}>
                        <Form.Item
                            label="Email"
                            name="email"
                            rules={[{ type: 'email', message: 'Некорректный email' }]}
                        >
                            <Input placeholder="name@example.com" allowClear />
                        </Form.Item>
                    </Col>
                </Row>

                <Divider style={{ margin: '12px 0' }} />

                <Row gutter={16} align="middle">

                </Row>
            </Form>
        </Modal>
    )
}
