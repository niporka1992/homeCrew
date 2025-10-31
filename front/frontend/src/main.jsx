import React from 'react'
import ReactDOM from 'react-dom/client'
import { ConfigProvider, theme, App as AntdApp } from 'antd'
import AppRouter from './router/AppRouter'

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: {
                    colorPrimary: '#1677ff',
                    borderRadius: 8,
                },
            }}
        >
            {/* ВАЖНО: Оборачиваем всё приложение */}
            <AntdApp>
                <AppRouter />
            </AntdApp>
        </ConfigProvider>
    </React.StrictMode>
)
