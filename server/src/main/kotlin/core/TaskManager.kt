package core

import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveAction
import java.util.concurrent.RecursiveTask

class TaskManager(val cm: CollectionManager, val um: UserManager, val port: Int) {
    private val socket = DatagramSocket(port, InetAddress.getLocalHost())

    class Packet(var host: InetAddress, var port: Int, var message: String = "")

    inner class ProcessTask(val list: List<Packet>): RecursiveTask<List<Packet>>() {
        override fun compute(): List<Packet> {
            when (list.size) {
                0 -> return ArrayList()
                1 -> {
                    val packet = list[0]
                    val ci = CommandInvoker(cm, um)
                    val cr = ConnectionReceiver(ci, socket)
                    val result = cr.process(packet.message)
                    packet.message = result
                    return listOf(packet)
                }
                else -> {
                    var list1 = list.dropLast(list.size / 2)
                    var list2 = list.drop(list1.size)
                    val task1 = ProcessTask(list1)
                    val task2 = ProcessTask(list2)
                    task1.fork()
                    task2.fork()
                    list1 = task1.join()
                    list2 = task2.join()
                    return list1 + list2
                }
            }
        }
    }

    inner class SendTask(val list: List<Packet>): RecursiveAction() {
        override fun compute() {
            when (list.size) {
                0 -> return
                1 -> {
                    val packet = list[0]
                    val ci = CommandInvoker(cm, um)
                    val cr = ConnectionReceiver(ci, socket)
                    cr.send(packet.host, packet.port, packet.message)
                }
                else -> {
                    val list1 = list.dropLast(list.size / 2)
                    val list2 = list.drop(list1.size)
                    val task1 = SendTask(list1)
                    val task2 = SendTask(list2)
                    task1.fork()
                    task2.fork()
                    task1.join()
                    task2.join()
                }
            }
        }
    }

    var received = ArrayList<Packet>()
    var toSend = ArrayList<Packet>()

    val receive = Thread {
        val rec = {
            val ci = CommandInvoker(cm, um)
            val cr = ConnectionReceiver(ci, socket)
            val packet = cr.receive()
            received.add(packet)
        }
        val pool = Executors.newFixedThreadPool(3)
        var t1 = pool.submit(rec)
        var t2 = pool.submit(rec)
        var t3 = pool.submit(rec)
        while (!Thread.currentThread().isInterrupted) {
            if (t1.isDone) t1 = pool.submit(rec)
            if (t2.isDone) t2 = pool.submit(rec)
            if (t3.isDone) t3 = pool.submit(rec)
        }
        pool.shutdownNow()
        val ci = CommandInvoker(cm, um)
        val cr = ConnectionReceiver(ci, socket)
        cr.send(socket.localAddress, port, "")
        cr.send(socket.localAddress, port, "")
        cr.send(socket.localAddress, port, "")
    }

    val process = Thread {
        val pool = ForkJoinPool.commonPool()
        while (!Thread.currentThread().isInterrupted) {
            if (received.isEmpty()) continue
            var list = received.toList()
            received.clear()
            list = pool.invoke(ProcessTask(list))
            toSend += list
        }
        pool.shutdownNow()
    }

    val send = Thread {
        val pool = ForkJoinPool.commonPool()
        while (!Thread.currentThread().isInterrupted) {
            if (toSend.isEmpty()) continue
            val list = toSend.toList()
            toSend.clear()
            pool.invoke(SendTask(list))
        }
        pool.shutdownNow()
    }

    fun start() {
        receive.start()
        process.start()
        send.start()
    }

    fun join() {
        receive.interrupt()
        process.interrupt()
        send.interrupt()
    }
}