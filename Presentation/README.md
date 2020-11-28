## ABS (adaptive bitrate streaming)

​		流媒体客户端首先获取所有码率的切片索引信息。一开始，客户端先请求最低码率的流。如果客户端判断下载速度比当前码率的切片流快，它就去请求下一个更高码率的流。随着播放的进行，如果客户端发现下载速度比当前码率的切片流慢，转而请求下一个较低码率的流。

- RTSP/RTCP
- HTTP-based video streaming

### Feature

- 使用HTTP传送视频流
- 使用多码率编码源内容
- 每个单码率的流被切成小的，几秒钟的小切片

### Tip

有必要区分ABR和这篇论文所做的事情的区别，ABR在只考虑一个用户的情况下，尽可能的优化个人QoE。而此片论文着重于讨论多个用户的总体体验。

**Connection-Level Fairness**: 所有人获得相同的带宽并不一定是最优的，尽可能按需分配是更好的。

## Minerva

### Goal

- 采用去中心化的方式去部署，部署在客户端。

- 码率与人的观感的函数取决于具体的图像。

- 客户端应当利用当前的状态，给出更好的算法。

- 每个客户端随机一个权重$w_i$, 传输层根据该权重进行分配()。
    ```
    Use Cubic as its underlying congestion control algorithm for achieving weighted bandwidth allocation
    ```

- 与标准的TCP进行合理竞争，与其他流量公平分配。

### QoE Fairness

$$
\mathrm{QoE}(c_k,R_k,c_{k-1})=P(c_k)-\beta R_k-\gamma||P(c_k)-P(c_{k-1})||
$$

$P(e):$  比特率和视频质量的关系函数。

$R_k:$ 可以认为视频的缓存用完了，需要卡一下用的时间。

PS: 这个式子的意思是，$R_k$肯定会带来负的贡献，而前后两个视频质量的差异过大也会带来不好的体验。

### Design

### Formulating the bandwidth utility

QoE函数太复杂，我们需要将其修改为只和下载速率有关的函数$U(r)$表示给定速率$r$,期望的QoE，这个才做每隔T秒做一次。

bitrate是离散的。考虑视频已经缓存了一段，那么QoE函数将只有一个变量$e_i$，这里$U$的构造就是对相邻点进行插值。然后整个函数将之和下载速率有关。

### Fix

$$
U(r)=\frac {\varphi_1(\text{Past QoE})+\varphi_2(\text{QoE from current chunk})+V_h(r,b,c_i)} {1+\varphi_1+\varphi_2}
$$

$\text{Past QoE}$: ...

$\text{QoE from current chunk}:$ 利用稳定状态的特性，rebuffer time is steady.

$V_h$: future QoE using ABS.

### Fairness with TCP?

Replace $U(r)$ to $f(U(r))$, $f$是单调递增的。

多视频提供商依然是公平的，如果他们共享链路，他们各自归一化后，将会公平共享链路流量。

大概意思就是虽然使用那个复杂的函数进行归一化很难，但是使用Basic的却很简单，为了让Client不获取到过多的信息，我们将使用用户索引改成了使用视频索引。（真是诡异的做法），然后强行解释了一通这样做是不错的。。。。。。。。。



## Outline

- Goal
  - Fairness between client
  - Deploy at client
  - State relative algorithm
  - Fairness with other TCP
- Observation
  - PQ function
  - Stateless problem
  - ....
- Design
  - Basic function / Client Aware function
  - Normalization function
- Optimization

