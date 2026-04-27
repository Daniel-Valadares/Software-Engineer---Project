# EcoCycle Manager - Sistema de Gestão de Logística Reversa (ODS 12)

Este repositório contém o desenvolvimento do projeto prático para a disciplina de Engenharia de Software. O foco da aplicação é atender ao **Objetivo de Desenvolvimento Sustentável 12 (Consumo e Produção Responsáveis)**, promovendo a gestão eficiente de resíduos tecnológicos.

---

## 1. Objetivo
O **EcoCycle Manager** tem como objetivo automatizar o controle do ciclo de vida de ativos de hardware em instituições, facilitando a identificação de itens obsoletos e garantindo que o processo de logística reversa e descarte ambientalmente correto seja seguido integralmente.

## 2. O Problema
Muitas organizações sofrem com o acúmulo de "lixo eletrônico" (e-waste) por falta de um inventário que monitore a saúde dos ativos. Isso gera dois problemas principais:
1. **Ambiental:** Descarte incorreto de metais pesados e componentes tóxicos.
2. **Segurança:** Descarte de dispositivos de armazenamento sem o devido processo de sanitização de dados.

## 3. Tipo de Solução
A solução será uma **Aplicação Desktop** desenvolvida com foco em arquitetura modular, utilizando persistência em banco de dados relacional local (**SQLite**). A escolha visa garantir que a ferramenta seja autossuficiente, rápida e de fácil instalação em ambientes administrativos.

---

## 4. Documentação

| Documento | Descrição |
| :--- | :--- |
| [📐 Arquitetura do Sistema](./docs/architecture.md) | Modelo C4 completo (Contexto, Container, Componente e Código), escolhas de tecnologias e justificativas arquiteturais |

---

## 5. Requisitos do Sistema

### 5.1 Requisitos Funcionais (RF)

| ID | Requisito | Descrição |
| :--- | :--- | :--- |
| **RF01** | Cadastro de Ativos | O sistema deve permitir o registro de itens com ID único, Categoria, Data de Aquisição e Prazo de Vida Útil. |
| **RF02** | Monitoramento de Ciclo | O sistema deve calcular o tempo restante de uso e sinalizar ativos próximos do vencimento técnico. |
| **RF03** | Checklist de Descarte | O sistema deve exigir a confirmação de etapas de segurança (limpeza de disco e remoção de baterias) antes de baixar um item. |
| **RF04** | Registro de Destinação | Deve ser possível registrar para qual entidade ou cooperativa o resíduo foi enviado. |
| **RF05** | Relatório de Sustentabilidade | Gerar um relatório consolidado com o peso total de material desviado de aterros comuns. |

### 5.2 Requisitos Não Funcionais (RNF)

| ID | Requisito | Categoria | Descrição |
| :--- | :--- | :--- | :--- |
| **RNF01** | Persistência Local | Armazenamento | O sistema utilizará SQLite para garantir que os dados sejam salvos localmente em um arquivo único. |
| **RNF02** | Padrão Arquitetural | Estruturação | A aplicação deve implementar o padrão MVC (Model-View-Controller) para separação de responsabilidades. |
| **RNF03** | Interface Gráfica | Usabilidade | A interface deve ser intuitiva, utilizando componentes visuais (JavaFX ou WPF) para facilitar a gestão do inventário. |
| **RNF04** | Performance | Eficiência | O carregamento de listas de ativos deve ser otimizado para não travar a interface durante a busca. |

---

## 6. Diagrama de Caso de Uso

O sistema possui dois perfis principais de interação: o **Gestor de TI**, responsável pela manutenção do inventário, e o **Auditor Ambiental**, que valida os relatórios de descarte.

![Diagrama de Caso de Uso - EcoCycle Manager](./docs/imagens/caso_de_uso.png)

---

## 7. Planejamento de Projeto

O acompanhamento das tarefas é realizado através do **GitHub Projects**, organizado da seguinte forma:

* **Project Backlog:** Contém todos os Requisitos Funcionais listados acima.
* **TODO (Sprint TP2):** Atividades focadas no Projeto de Software e Arquitetura (C4 Model).
* **Doing:** Atividades em desenvolvimento na sprint atual.
* **Done:** Atividades validadas e concluídas.
