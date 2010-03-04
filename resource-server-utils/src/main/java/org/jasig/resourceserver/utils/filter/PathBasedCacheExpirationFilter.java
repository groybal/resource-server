package org.jasig.resourceserver.utils.filter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.GenericFilterBean;

public class PathBasedCacheExpirationFilter extends GenericFilterBean {

    protected final Log logger = LogFactory.getLog(this.getClass());
    private final DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", new Locale("en"));

	private Set<Integer> _maxAges;

	private Map<String,Integer> cacheMaxAges;
	
	public void setCacheMaxAges(Map<String,Integer> cacheMaxAges) {
		this.cacheMaxAges = cacheMaxAges;
		this._maxAges = new HashSet<Integer>();
		this._maxAges.addAll(cacheMaxAges.values());
	}
	
	private AntPathMatcher pathMatcher = new AntPathMatcher();
	
	private ConcurrentHashMap<Integer, String> cachedControlStrings = new ConcurrentHashMap<Integer, String>();
	private ConcurrentHashMap<Integer, String> cachedExpiresStrings = new ConcurrentHashMap<Integer, String>();

    private Timer headerUpdateTimer;
    
    //Default header cache time is 1 second
    private long regenerateHeadersInterval = 1000;

    public PathBasedCacheExpirationFilter() {
        final TimeZone timeZone = TimeZone.getTimeZone("GMT");
        this.dateFormat.setTimeZone(timeZone);
    }
    
    public long getRegenerateHeadersInterval() {
        return this.regenerateHeadersInterval;
    }
    /**
     * @param regenerateHeadersInterval The interval in milliseconds to regenerate the cache headers, defaults to 1 second (1000).
     */
    public void setRegenerateHeadersInterval(long regenerateHeadersInterval) {
        if (regenerateHeadersInterval < 1) {
            throw new IllegalArgumentException("'regenerateHeadersInterval' must be greater than 0, (" + regenerateHeadersInterval + ")");
        }
        
        this.regenerateHeadersInterval = regenerateHeadersInterval;
    }

    /* (non-Javadoc)
     * @see org.springframework.web.filter.GenericFilterBean#initFilterBean()
     */
    @Override
    protected void initFilterBean() throws ServletException {
        //Generate cache control values
    	for (Integer age : _maxAges){
            this.cachedControlStrings.put(age, "public, max-age=" + age);
    	}
        
        //Initialize cache header
        this.updateCacheHeaders();
        
        //Start timer to periodically refresh the cache header
        final ServletContext servletContext = this.getServletContext();
        final String servletContextPath = servletContext.getContextPath();
        this.headerUpdateTimer = new Timer(servletContextPath + "-CacheHeaderUpdateTimer", true);
        this.headerUpdateTimer.schedule(new CacheHeaderUpdater(), this.regenerateHeadersInterval, this.regenerateHeadersInterval);
    }
    
    /**
	 * {@inheritDoc}
	 */
    @Override
	public void destroy() {
	    this.headerUpdateTimer.cancel();
	    this.headerUpdateTimer = null;
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

	    // add the cache expiration time to the response
        if (response instanceof HttpServletResponse && request instanceof HttpServletRequest) {
			final HttpServletResponse httpResponse = (HttpServletResponse) response;
			
			String path = ((HttpServletRequest)request).getServletPath();
			
			for (Entry<String,Integer> entry : cacheMaxAges.entrySet()) {

				if (pathMatcher.match(entry.getKey(), path)) {

					final String expires = this.getExpiresHeader(entry.getValue());
					httpResponse.setHeader("Expires", expires);
					
					httpResponse.setHeader("Cache-Control", this.cachedControlStrings.get(entry.getValue()));
					
					break;
				}
			}
		}
		
		// continue
		chain.doFilter(request, response);
	}

	protected String getExpiresHeader(Integer cacheMaxAge) {
	    return this.cachedExpiresStrings.get(cacheMaxAge);
	}

    protected void updateCacheHeaders() {
    	for (Integer cacheMaxAge : this._maxAges) {
            synchronized (this.dateFormat) {
                final Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, cacheMaxAge);
                this.cachedExpiresStrings.put(cacheMaxAge, this.dateFormat.format(cal.getTime()));
            }
    	}
    }
    
    /**
     * Simple task that calls {@link CacheExpirationFilter#updateCacheHeader()}
     */
    private final class CacheHeaderUpdater extends TimerTask {
        /* (non-Javadoc)
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run() {
            updateCacheHeaders();
        }
    }

}