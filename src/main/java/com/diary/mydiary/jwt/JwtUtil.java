package com.diary.mydiary.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil
{
    // 🔐 비밀 키 (실제 운영에서는 환경변수로 관리!)
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 🔐 토큰 유효 기간 (30분)
    private final long expiration = 1000 * 60 * 30;

    // ✅ 토큰 생성
    public String createToken(String username)
    {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    // ✅ 토큰에서 사용자명 꺼내기
    public String getUsername(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // ✅ 토큰 검증
    public boolean validateToken(String token)
    {
        try
        {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e)
        {
            return false;
        }
    }
}
