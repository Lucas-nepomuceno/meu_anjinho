# Meu Anjinho: App Android

&emsp; Este repositório contém o código para o aplicativo Meu Anjinho. A proposta é um aplicativo para Android que permite aos papais guardar registros dos filhos. A tentativa é imitar os famosos álbums de família, mantendo registros e conquistas dos bebês. A proposta do aplicativo é que tudo seja salvo localmente no dispositivo. No entanto, é possível "Salvar backup online". Para isso, você envia seu email e o backend da aplicação te manda um código para recuperar os registros.

&emsp; Esse aplicativo foi criado para cumprir a atividade ponderada do módulo 10 de Engenharia da Computação. No entanto, tem tudo a ver com o meu momento de vida atual, com a chegada da primeira filha: Sofia Nepomuceno.

## Demonstração do aplicativo

&emsp; O vídeo de demonstração de uso do aplicativo está neste link:

## Tecnologias utilizadas

### Aplicativo Android

- Kotlin: Linguagem principal do app Android.

- Jetpack Compose: Usado para construir as telas, como:
 - HomeScreen
 - RegistrosScreen
 - AdicionarRegistroScreen
 - DetalheRegistroScreen

- Material 3: Componentes visuais como:
 - Scaffold
 - TopAppBar
 - FloatingActionButton
 - Button
 - OutlinedButton
 - AlertDialog
 - Card
 - TextField
 - ModalBottomSheet

- Room Database: Banco local do app, usado para salvar:
 - Crianças
 - Registros
 - Caminhos das fotos associadas

Activity Result API: Usada para:
 - Selecionar fotos da galeria
 - Tirar foto com a câmera
 - Pedir permissão de notificação

- WorkManager: Usado para agendar a notificação diária e o lembrete de 6 horas.

- Retrofit + OkHttp: Usados para comunicação com o backend:
 - Criar backup
 - Enviar registros
 - Enviar fotos
 - Restaurar backup futuramente

### Backend

- Go / Golang: Linguagem usada para criar o servidor da API.

- net/http: Biblioteca padrão do Go para criar rotas HTTP.

- SQLite: Banco de dados escolhido para facilitar o desenvolvimento local

- Armazenamento local de arquivos: As fotos do backup ficam salvas em uma pasta do servidor.

## Instruções para rodar o aplicativo Meu Anjinho

### Pré-Requisitos

Antes de rodar o projeto, instale:
- Android Studio
- JDK 17 ou superior
- Go
- SQLite
- Celular Android ou emulador

### 1. Rodar o backend

Entre na pasta do backend:

```bash
cd backend
```

Instale as dependências do Go:

```bash
go mod tidy
```

Rode o servidor:

```bash
go run ./cmd/api
```

Se tudo estiver certo, aparecerá algo parecido com:

```text
Servidor rodando na porta 8080
```

O backend ficará disponível em:

```text
http://localhost:8080
```

### 2. Banco de dados do backend

Ao iniciar o servidor, o arquivo do banco será criado automaticamente:

```text
meuanjinho.db
```

Também será criada a pasta para salvar os arquivos enviados no backup:

```text
storage/
```


### 3. Configurar o IP do backend no app Android

Se estiver usando **emulador Android**, no arquivo `RetrofitClient.kt`, use:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"
```

Se estiver usando **celular físico**, use o IP do seu computador na rede Wi-Fi.

No Windows, rode:

```bash
ipconfig
```

Procure o IPv4 da sua rede Wi-Fi, por exemplo:

```text
192.168.0.105
```

Então configure:

```kotlin
private const val BASE_URL = "http://192.168.0.105:8080/"
```

O celular e o computador precisam estar na mesma rede Wi-Fi.


### 4. Rodar o aplicativo Android

Abra a pasta do app no **Android Studio**.

Aguarde o Gradle sincronizar.

Depois selecione:

```text
Emulador Android
ou
Celular físico conectado via USB
```

Clique em:

```text
Run
```

ou pressione:

```text
Shift + F10
```


### 5. Permissões necessárias

Ao abrir o aplicativo, permita:

```text
Notificações
Câmera
Acesso às fotos selecionadas
```

Essas permissões são usadas para:

```text
Enviar lembretes diários
Tirar fotos
Selecionar imagens da galeria
Salvar registros com fotos
```


### 6. Testar o backup online

Com o backend rodando, abra o app e clique em:

```text
Salvar backup online
```

Digite um email.

O app deve enviar os registros e fotos para o backend e retornar um código parecido com:

```text
ABCD-1234-EFGH
```

Guarde esse código, pois ele será usado futuramente para restaurar o backup.

---

### 7. Testar o backend manualmente

Com o servidor rodando, teste criando um backup:

```bash
curl -X POST http://localhost:8080/api/v1/backups ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"teste@email.com\"}"
```

No PowerShell:

```powershell
curl -Method POST "http://localhost:8080/api/v1/backups" `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '{"email":"teste@email.com"}'
```

Resposta esperada:

```json
{
  "backupCode": "ABCD-1234-EFGH"
}
```