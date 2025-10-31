import React, { useState } from 'react'
import { Form, Input, Alert, Typography } from 'antd'
import { isValidCron } from 'cron-validator'
import cronstrue from 'cronstrue'
import 'cronstrue/locales/ru'

const { Text } = Typography

export default function CustomCronMode() {
    const [description, setDescription] = useState('')

    const handleChange = (e) => {
        const value = e.target.value
        if (isValidCron(value, { seconds: true, alias: true, allowBlankDay: true })) {
            setDescription(cronstrue.toString(value, { locale: 'ru' }))
        } else {
            setDescription('')
        }
    }

    return (
        <>
            <Alert
                message="Введите собственное CRON-выражение. Будет выполнена валидация и показана расшифровка."
                type="info"
                showIcon
                style={{ marginBottom: 12 }}
            />
            <Form.Item
                label="CRON-выражение"
                name="customCron"
                extra="Пример: 0 30 8 ? * MON,WED,FRI — запуск по пн, ср и пт в 08:30"
                rules={[
                    { required: true, message: 'Введите CRON-выражение' },
                    {
                        validator: (_, value) => {
                            if (!value) return Promise.resolve()
                            const valid = isValidCron(value, { seconds: true, alias: true, allowBlankDay: true })
                            return valid
                                ? Promise.resolve()
                                : Promise.reject(new Error('Некорректное CRON-выражение'))
                        },
                    },
                ]}
            >
                <Input placeholder="0 30 8 ? * MON,WED,FRI" onChange={handleChange} />
            </Form.Item>

            {description && (
                <Text type="secondary" style={{ display: 'block', marginTop: 8 }}>
                    🕓 Расшифровка: {description}
                </Text>
            )}
        </>
    )
}
