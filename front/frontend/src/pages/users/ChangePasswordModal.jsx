import { Form, Grid, Input, Modal, theme, Typography } from 'antd'
import { useEffect } from 'react'

const { Text } = Typography
const { useBreakpoint } = Grid

export default function ChangePasswordModal({ open, user, saving, onCancel, onSave }) {
    const [form] = Form.useForm()
    const { token } = theme.useToken()
    const screens = useBreakpoint()
    const isMobile = !screens.md

    useEffect(() => {
        if (open) form.resetFields()
    }, [open, form])

    const handleOk = async () => {
        try {
            const { newPassword } = await form.validateFields()
            await onSave(newPassword)
        } catch {
            // Ошибки уже будут показаны через antd
        }
    }

    return (
        <Modal
            width={isMobile ? 340 : 420}
            title={
                <Text
                    strong
                    style={{
                        color: token.colorPrimaryText,
                        fontSize: isMobile ? 16 : 18,
                    }}
                >
                    🔒 Смена пароля {user ? `для ${user.username}` : ''}
                </Text>
            }
            open={open}
            onCancel={onCancel}
            onOk={handleOk}
            okText="Сменить пароль"
            confirmLoading={saving}
            destroyOnClose
            centered
            maskClosable={false}
            styles={{
                content: {
                    background: token.colorBgElevated,
                    borderRadius: 12,
                    boxShadow: '0 4px 24px rgba(0,0,0,0.45)',
                    padding: isMobile ? '16px 20px' : '20px 24px',
                },
                header: {
                    background: token.colorBgElevated,
                    borderBottom: `1px solid ${token.colorBorderSecondary}`,
                },
                footer: {
                    background: token.colorBgElevated,
                    borderTop: `1px solid ${token.colorBorderSecondary}`,
                },
            }}
        >
            <Form
                form={form}
                layout="vertical"
                requiredMark={false}
                style={{
                    marginTop: 8,
                    color: token.colorTextSecondary,
                    display: 'flex',
                    justifyContent: 'center',
                }}
            >
                <Form.Item
                    name="newPassword"
                    style={{ width: '80%', maxWidth: 260 }}
                    rules={[
                        { required: true, message: 'Введите новый пароль' },
                        {
                            min: 8,
                            max: 64,
                            message: 'Пароль должен быть от 8 до 64 символов',
                        },
                        {
                            pattern: /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&()№]).+$/,
                            message:
                                'Пароль должен содержать заглавную, строчную буквы, цифру и спецсимвол',
                        },
                    ]}
                >
                    <Input.Password
                        placeholder="Введите новый пароль"
                        autoFocus
                        maxLength={64}
                        style={{
                            background: token.colorBgContainer,
                            borderColor: token.colorBorder,
                            color: token.colorText,
                            width: '100%',
                            textAlign: 'center',
                        }}
                    />
                </Form.Item>
            </Form>
        </Modal>
    )
}
