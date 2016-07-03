/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2016 Valentin 'ThisIsMac' Marchaud
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package fr.vmarchaud.mineweb.bukkit;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import fr.vmarchaud.mineweb.common.IBaseMethods;
import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.injector.NettyInjector;
import fr.vmarchaud.mineweb.common.injector.router.RouteMatcher;
import fr.vmarchaud.mineweb.utils.CustomLogFormatter;
import fr.vmarchaud.mineweb.utils.Handler;
import fr.vmarchaud.mineweb.utils.http.HttpResponseBuilder;
import fr.vmarchaud.mineweb.utils.http.RoutedHttpRequest;
import fr.vmarchaud.mineweb.utils.http.RoutedHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

public class BukkitCore extends JavaPlugin implements ICore {
	
	public static ICore		instance;
	public static ICore get() {
		return instance;
	}
	
	
	private RouteMatcher			httpRouter;
	private NettyInjector			injector;
	
	/** Cached player list to not rely on Reflection on every request **/
	private HashSet<String>			players;
	
	private IBaseMethods			methods;
	private Logger					logger		= Logger.getLogger("Mineweb");
	
	@Override
	public void onEnable() {
		instance = this;
		// directly setup logger
		setupLogger();
		
		// Init
		logger.info("Loading ...");
		injector = new BukkitNettyInjector(this);
		httpRouter = new RouteMatcher();
		methods = new BukkitBaseMethods(instance);
		logger.info("Registering route ...");
		registerRoutes();
		getServer().getPluginManager().registerEvents(new BukkitListeners(instance), this);
		
		
		// inject when we are ready
		logger.info("Injecting http server ...");
		injector.inject();
		logger.info("Ready !");
	}

	public void registerRoutes() {
		httpRouter.everyMatch(new Handler<Void, RoutedHttpResponse>() {
			
			@Override
			public Void handle(RoutedHttpResponse event) {
				logger.fine("[HTTP Request] " + event.getRes().getStatus().code() + " " + event.getRequest().getMethod().toString() + " " + event.getRequest().getUri());
				return null;
			}
		});
		
		httpRouter.get("/", new Handler<FullHttpResponse, RoutedHttpRequest>() {
            @Override
            public FullHttpResponse handle(RoutedHttpRequest event) {
                return new HttpResponseBuilder().text("mineweb_bridge").build();
            }
        });
	}
	
	public void setupLogger() {
		try {
			logger.setUseParentHandlers(false);
			FileHandler		fileHandler = new FileHandler(getDataFolder() + "/" + this.getDescription().getName() + "/" + "mineweb.log");
			fileHandler.setFormatter(new CustomLogFormatter());
			logger.addHandler(fileHandler);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public RouteMatcher getHTTPRouter() {
		return httpRouter;
	}

	@Override
	public Object getPlugin() {
		return this;
	}

	@Override
	public EnumPluginType getType() {
		return EnumPluginType.BUKKIT;
	}

	@Override
	public Object getGameServer() {
		return this.getServer();
	}

	@Override
	public HashSet<String> getPlayers() {
		return players;
	}
	
	@Override
	public Logger logger() {
		return logger;
	}

}
