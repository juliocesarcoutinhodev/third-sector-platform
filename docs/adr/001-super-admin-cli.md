# ADR-001: Criacao de Super Admin via CLI em vez de endpoint HTTP

**Status:** Aceito

**Data:** 2026-06-28

**Contexto:**

A plataforma precisa permitir a criacao de usuarios com papel SUPER_ADMIN, que possuem
acesso irrestrito a todos os tenants e operacoes do sistema. Esta e a credencial de
mais alto privilegio da plataforma.

Duas abordagens foram consideradas:

1. **Endpoint HTTP** (`POST /api/super-admin`) protegido por autenticacao previa
2. **Comando CLI** executado diretamente no ambiente de deploy

**Decisao:**

Implementar a criacao de Super Admin **exclusivamente via comando de linha de comando**
(`--create-super-admin --email=admin@exemplo.com`), sem qualquer endpoint HTTP.

**Racional:**

1. **Reducao de superficie de ataque:** Um endpoint HTTP para criar Super Admins,
   mesmo que protegido por autenticacao, estaria exposto a internet e sujeito a:
   - Ataques de forca bruta
   - Exploracao de vulnerabilidades de autenticacao/autorizacao
   - Vazamento acidental de credenciais via logs de proxy/load balancer
   - CSRF e outras tecnicas de ataque web

2. **Principio do menor privilegio:** A criacao do Super Admin e uma operacao que
   deve ser realizada apenas uma vez (ou rarissimas vezes) durante o bootstrap da
   plataforma. Nao ha necessidade de expo-la como uma API sempre disponivel.

3. **Seguranca da senha temporaria:** A senha e gerada internamente e exibida
   apenas no console/log de saida do comando. Nao trafega pela rede HTTP e nao
   aparece em nenhum argumento de linha de comando (evitando `ps aux` e historico
   de shell).

4. **Auditabilidade:** A execucao do comando CLI fica registrada nos logs do
   servidor, criando uma trilha de auditoria de quem criou o Super Admin e quando.

5. **Simplicidade operacional:** Nao requer configuracao adicional de seguranca
   (firewall, rate limiting, etc.) que seria necessaria para proteger um endpoint
   HTTP dessa criticidade.

**Consequencias:**

- A criacao de Super Admins requer acesso ao ambiente de execucao (SSH, console)
- Nao e possivel criar Super Admins remotamente sem acesso ao servidor
- Operacao nao escalavel horizontalmente — o que e desejavel para este caso de uso
- O DevDataSeeder tambem utiliza o mesmo use case, com senha fixa e documentada
  para o ambiente de desenvolvimento

**Alternativas consideradas e rejeitadas:**

- **Endpoint HTTP com autenticacao forte (mTLS + token):** Adiciona complexidade
  operacional e ainda mantem uma superficie de ataque exposta
- **Seed manual no banco:** Mais propenso a erros e nao gera senha segura
- **Variavel de ambiente com senha pre-definida:** Exporia a senha no ambiente

**Referencias:**

- OWASP: "Minimize the attack surface area"
- NIST SP 800-63B: "Memorized Secret Verifiers"
