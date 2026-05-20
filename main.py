import os
import re

TARGET_DIR = './app'
# Регулярка для //, /* */ и комментариев
COMMENT_PATTERN = re.compile(r'//.*|/\*(?:.|[\n\r])*?\*/|')

print(f"[*] Запуск очистки комментариев в папке: {TARGET_DIR}")

for root, dirs, files in os.walk(TARGET_DIR):
    for file in files:
        # Проверяем только нужные расширения файлов Android-проекта
        if file.endswith(('.kt', '.java', '.gradle', '.xml')):
            file_path = os.path.join(root, file)
            
            try:
                # Читаем содержимое файла
                with open(file_path, 'r', encoding='utf-8') as f:
                    original_content = f.read()
                
                # Ищем и удаляем комментарии
                cleaned_content, count = COMMENT_PATTERN.subn('', original_content)
                
                # Если что-то нашли и удалили — перезаписываем файл
                if count > 0:
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(cleaned_content)
                    print(f"[+] Вырезано {count} коммент. из: {file_path}")
                    
            except Exception as e:
                print(f"[!] Ошибка при обработке {file_path}: {e}")

print("[*] Очистка успешно завершена!")