# spring-ai-aliyun-dashscope-spring-boot-starter

> 基于 [阿里云灵积模型服务平台](https://dashscope.aliyun.com/) 和 Spring AI 的 Spring Boot Starter 实现

### 阿里云灵积模型服务平台

> DashScope灵积模型服务建立在“模型即服务”（Model-as-a-Service，MaaS）的理念基础之上，围绕AI各领域模型，通过标准化的API提供包括模型推理、模型微调训练在内的多种模型服务。
</br> 通过围绕模型为中心，DashScope灵积模型服务致力于为AI应用开发者提供品类丰富、数量众多的模型选择，并通过API接口为其提供开箱即用、能力卓越、成本经济的模型服务。各领域模型的能力均可通过DashScope统一的API和SDK来实现被不同业务系统集成，AI应用开发和模型效果调优的效率将因此得以激发，助力开发者释放灵感、创造价值。

- 官网地址：[https://dashscope.aliyun.com/](https://dashscope.aliyun.com/)
- 模型列表：[https://help.aliyun.com/zh/dashscope/developer-reference/model-square/](https://help.aliyun.com/zh/dashscope/developer-reference/model-square/)
- API文档：[https://help.aliyun.com/zh/dashscope/developer-reference/api-details](https://help.aliyun.com/zh/dashscope/developer-reference/api-details)
- 体验中心: [https://tongyi.aliyun.com/qianwen/](https://tongyi.aliyun.com/qianwen/)

#### 通义千问 - 大语言模型

> 通义千问是由阿里云自主研发的大语言模型，用于理解和分析用户输入的自然语言，在不同领域、任务内为用户提供服务和帮助。您可以通过提供尽可能清晰详细的指令，来获取符合您预期的结果。

##### 应用场景

> 通义千问凭借其强大的语言处理能力，为用户带来高效、智能的语言服务体验，其能力包括但不限于文字创作、翻译服务和对话模拟等，具体应用场景如下：

- 文字创作：撰写故事、公文、邮件、剧本和诗歌等。
- 文本处理：润色文本和提取文本摘要等。
- 编程辅助：编写和优化代码等。
- 翻译服务：提供各类语言的翻译服务，如英语、日语、法语或西班牙语等。
- 对话模拟：扮演不同角色进行交互式对话。
- 数据可视化：图表制作和数据呈现等。

##### 支持的功能包括：

- 支持单轮对话（Chat Completion API）
- 支持多轮对话（Chat Completion API），支持返回流式输出结果
- 支持函数调用（Function Calling）：用户传入各类自定义工具，自动选择并调用工具，准确度达到99%
- 支持文本嵌入（Embeddings）
- 支持图片生成（Image Generation API）
- 支持模型调优（Fine-tuning API）
- ...

##### 资源

- [查看模型列表](https://help.aliyun.com/zh/dashscope/developer-reference/model-square/?spm=a2c4g.11186623.0.0.18f21418x9E1bJ)
- [API 介绍](https://help.aliyun.com/zh/dashscope/developer-reference/api-details?spm=a2c4g.11186623.0.0.587fe0f6sxPru7)

#### 通义千问 - 视觉理解大模型

> 通义千问开源视觉理解大模型Qwen-VL于2023年12月1日发布重大更新，不仅大幅提升通用OCR、视觉推理、中文文本理解基础能力，还能处理各种分辨率和规格的图像，甚至能“看图做题”。

升级的 Qwen-VL(qwen-vl-plus/qwen-vl-max) 模型现有几大特点：

- 大幅增强了图片中文字处理能力，能够成为生产力小帮手，提取、整理、总结文字信息不在话下。
- 增加可处理分辨率范围，各分辨率和长宽比的图都能处理，大图和长图能看清。
- 增强视觉推理和决策能力，适于搭建视觉Agent，让大模型Agent的想象力进一步扩展。
- 升级看图做题能力，拍一拍习题图发给Qwen-VL，大模型能帮用户一步步解题。

##### 资源

- [API 介绍](https://help.aliyun.com/zh/dashscope/developer-reference/tongyi-qianwen-vl-plus-api)

#### 通义千问 - 大规模音频语言模型

> 通义千问Audio是阿里云研发的大规模音频语言模型。通义千问Audio可以以多种音频 (包括说话人语音、自然音、音乐、歌声）和文本作为输入，并以文本作为输出。通义千问Audio模型的特点包括：

- 1、全类型音频感知：通义千问Audio是一个性能卓越的通用音频理解模型，支持30秒内的自然音、人声、音乐等类型音频理解，如多语种语音识别，时间抽定位，说话人情绪、性别识别，环境识别，音乐的乐器、风格、情感识别等。
- 2、基于音频推理：通义千问Audio支持基于音频内容进行相关推理和创作，如语义理解，场景推理，相关推荐，内容创作等。
- 3、支持多轮音频和文本对话：通义千问Audio支持多音频分析、多轮音频-文本交错对话。

##### 资源

- [API 介绍](https://help.aliyun.com/zh/dashscope/developer-reference/qwen-audio-api)

### Maven

``` xml
<dependency>
	<groupId>com.github.hiwepy</groupId>
	<artifactId>spring-ai-aliyun-dashscope-spring-boot-starter</artifactId>
	<version>${project.version}</version>
</dependency>
```


### Sample

使用示例请参见 [Spring AI Examples](https://github.com/TeachingAI/spring-ai-examples)

### License

[Apache License 2.0](LICENSE)
